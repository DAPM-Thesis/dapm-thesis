package pipeline;

import candidate_validation.*;
import communication.API.*;
import communication.API.request.HTTPRequest;
import communication.API.request.PEInstanceRequest;
import communication.API.response.HTTPResponse;
import communication.API.response.PEInstanceResponse;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import controller.PipelineBuilderController.OperationalParamsRequest;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.HeartbeatTimingConfig;
import pipeline.processingelement.heartbeat.HeartbeatTopicConfig;
import pipeline.processingelement.heartbeat.UserDefinedHeartbeatConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import repository.PipelineRepository;
import utils.graph.DG;
import utils.JsonUtil;
import utils.LogUtil;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PipelineBuilder {
    private final HTTPClient webClient;
    private final PipelineRepository pipelineRepository;
    
    @Autowired
    public PipelineBuilder(HTTPClient webClient, PipelineRepository pipelineRepository) {
        this.webClient = webClient;
        this.pipelineRepository = pipelineRepository;
    }

    public void buildPipeline(String pipelineID, ValidatedPipeline validatedPipeline) {
        Pipeline pipeline = new Pipeline(pipelineID, validatedPipeline.getChannels(), validatedPipeline.getFaultToleranceLevel(), validatedPipeline.getUserDefinedHeartbeatConfig());
        buildPipeline(pipeline);
        pipelineRepository.storePipeline(pipelineID, pipeline);
        configureHeartbeats(pipeline);
        setOperationalParametersOnPEs(pipeline);
    }

    private void buildPipeline(Pipeline pipeline) {
        Map<ProcessingElementReference, PEInstanceResponse> configuredInstances = new HashMap<>();
        Set<ProcessingElementReference> currentLevel = pipeline.getSources();
        DG<ProcessingElementReference, Integer> directedGraph = pipeline.getDirectedGraph();

        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for (ProcessingElementReference pe : currentLevel) {
                if (configuredInstances.containsKey(pe)) continue;
                // Check if all upstream connections are configuredInstances yet
                if (!allInputsReady(directedGraph, configuredInstances, pe)) {
                    // Defer to next level if waiting for inputs
                    nextLevel.add(pe);
                    continue;
                }

                if (pe.isSource()) {
                    String instanceID = createSource(configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isOperator()) {
                    String instanceID = createOperator(directedGraph, configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isSink()) {
                    String instanceID = createSink(directedGraph, configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                }

                // Add all downstream nodes for the next level
                nextLevel.addAll(pipeline.getDirectedGraph().getDownStream(pe));
            }
            currentLevel = nextLevel;
        }
    }

    private boolean allInputsReady(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configured, ProcessingElementReference pe) {
        return directedGraph.getUpstream(pe).stream().allMatch(configured::containsKey);
    }

    private List<ConsumerConfig> getConsumerConfigs(DG<ProcessingElementReference, Integer> directedGraph, ProcessingElementReference pe, Map<ProcessingElementReference, PEInstanceResponse> configured) {
        return directedGraph.getUpstream(pe).stream()
                .filter(node -> {
                    PEInstanceResponse response = configured.get(node);
                    ProducerConfig producerConfig = response != null ? response.getProducerConfig() : null;
                    boolean hasEdgeAttribute = directedGraph.getEdgeAttribute(node, pe) != null;
                    return producerConfig != null
                            && hasEdgeAttribute
                            && producerConfig.brokerURL() != null && !producerConfig.brokerURL().isEmpty()
                            && producerConfig.topic() != null && !producerConfig.topic().isEmpty();
                })
                .map(node -> new ConsumerConfig(
                        configured.get(node).getProducerConfig().brokerURL(),
                        configured.get(node).getProducerConfig().topic(),
                        directedGraph.getEdgeAttribute(node, pe)))
                .collect(Collectors.toList());
    }

    private String createSource(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        PEInstanceResponse sourceResponse = sendCreateSourceRequest(pe);
        configuredInstances.put(pe, sourceResponse);
        return sourceResponse.getInstanceID();
    }

    private String createOperator(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(directedGraph, pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for operator: " + pe);
        }
        PEInstanceResponse operatorResponse = sendCreateOperatorRequest(pe, consumerConfigs);
        configuredInstances.put(pe, operatorResponse);
        return operatorResponse.getInstanceID();
    }

    private String createSink(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(directedGraph, pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for sink: " + pe);
        }
        PEInstanceResponse sinkResponse = sendCreateSinkRequest(pe, consumerConfigs);
        configuredInstances.put(pe, sinkResponse);
        return sinkResponse.getInstanceID();
    }

    private void configureHeartbeats(Pipeline pipeline) {
        DG<ProcessingElementReference, Integer> dg = pipeline.getDirectedGraph();
        LogUtil.info("[BUILDER HB] Starting Phase 1 Heartbeat configuration for pipeline: {}", pipeline.getPipelineID());

        if (pipeline.getProcessingElements().isEmpty()) {
            LogUtil.info("[BUILDER HB] No processing elements in the pipeline '{}' to configure heartbeats for.", pipeline.getPipelineID());
            return;
        }

        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String currentInstanceID = entry.getKey();
            ProcessingElementReference currentPERef = entry.getValue();

            HeartbeatTopicConfig configForCurrentPE = new HeartbeatTopicConfig();

            // 1. Define PE's OWN PUBLISH topics
            if (currentPERef.isOperator() || currentPERef.isSink()) {
                configForCurrentPE.setUpstreamHeartbeatPublishTopic("hb-upstream-" + currentInstanceID);
            }
            if (currentPERef.isSource() || currentPERef.isOperator()) {
                configForCurrentPE.setDownstreamHeartbeatPublishTopic("hb-downstream-" + currentInstanceID);
            }

            // 2. Determine topics of NEIGHBORS PE should subscribe to
            List<String> upstreamTopicsToConsumeFrom = new ArrayList<>();
            for (ProcessingElementReference upstreamNeighborRef : dg.getUpstream(currentPERef)) {
                String upstreamNeighborID = pipeline.getInstanceID(upstreamNeighborRef);
                if (upstreamNeighborID == null) continue;
                if (upstreamNeighborRef.isSource() || upstreamNeighborRef.isOperator()) { // downstream-facing
                    upstreamTopicsToConsumeFrom.add("hb-downstream-" + upstreamNeighborID);
                }
            }
            configForCurrentPE.setUpstreamNeighborHeartbeatTopicsToSubscribeTo(upstreamTopicsToConsumeFrom);

            List<String> downstreamTopicsToConsumeFrom = new ArrayList<>();
            for (ProcessingElementReference downstreamNeighborRef : dg.getDownStream(currentPERef)) {
                String downstreamNeighborID = pipeline.getInstanceID(downstreamNeighborRef);
                if (downstreamNeighborID == null) continue;
                if (downstreamNeighborRef.isOperator() || downstreamNeighborRef.isSink()) { // upstream-facing
                    downstreamTopicsToConsumeFrom.add("hb-upstream-" + downstreamNeighborID);
                }
            }
            configForCurrentPE.setDownstreamNeighborHeartbeatTopicsToSubscribeTo(downstreamTopicsToConsumeFrom);
            // Determine which upstream topics are optional
            Set<String> optionalTopics = new HashSet<>();
            for (ChannelReference channel : pipeline.getChannels()) {
                for (SubscriberReference subscriber : channel.getSubscribers()) {
                    if (subscriber.getElement().equals(currentPERef) && subscriber.isOptional()) {
                        String producerInstanceId = pipeline.getInstanceID(channel.getPublisher());
                        if (producerInstanceId != null) {
                            optionalTopics.add("hb-downstream-" + producerInstanceId);
                        }
                    }
                }
            }
            configForCurrentPE.setOptionalUpstreamNeighborTopics(optionalTopics);
            
            
            String url = currentPERef.getOrganizationHostURL() + "/pipelineBuilder/heartbeat/instance/" + currentInstanceID;
                HTTPRequest req = new HTTPRequest(url, JsonUtil.toJson(configForCurrentPE));
            
            LogUtil.info("[BUILDER HB] Sending config to PE {} ({}) at {}: UpPubT={}, DownPubT={}, MonUp#={}, MonDown#={}",
                    currentPERef.getTemplateID(), currentInstanceID, url,
                    configForCurrentPE.getUpstreamHeartbeatPublishTopic(), configForCurrentPE.getDownstreamHeartbeatPublishTopic(),
                    configForCurrentPE.getUpstreamNeighborHeartbeatTopicsToSubscribeTo().size(),
                    configForCurrentPE.getDownstreamNeighborHeartbeatTopicsToSubscribeTo().size());
            
            HTTPResponse resp = webClient.putSync(req);
            if (resp == null || !resp.status().is2xxSuccessful()) {
                String errorDetails = String.format("Status: %s, Body: %s, Sent: %s",
                    resp != null ? resp.status() : "N/A",
                    resp != null ? resp.body() : "N/A",
                    JsonUtil.toJson(configForCurrentPE));
                LogUtil.info("[BUILDER HB ERR] Failed to config HB for {} ({}). {}",
                        currentPERef.getTemplateID(), currentInstanceID, errorDetails);
                throw new IllegalStateException("Failed to configure Phase 1 heartbeat for " + currentInstanceID + ". " + errorDetails);
            } else {
                LogUtil.info("[BUILDER HB] Successfully configured Phase 1 heartbeat for PE {} ({})", currentPERef.getTemplateID(), currentInstanceID);
            }
        }
        LogUtil.info("[BUILDER HB] Phase 1 Heartbeat configuration dispatch completed for pipeline: {}", pipeline.getPipelineID());
    }

    private void setOperationalParametersOnPEs(Pipeline pipeline) {
        LogUtil.info("[BUILDER OP PARAMS] Setting operational parameters for pipeline: {}", pipeline.getPipelineID());
        
        HeartbeatTimingConfig finalHeartbeatConfig = getHeartbeatTimingConfigs(
                pipeline.getUserDefinedHeartbeatConfig(),
                pipeline.getProcessingElements().size()
        );

        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceID = entry.getKey();
            ProcessingElementReference peRef = entry.getValue();
            
            // TODO: Also need the pipeline owner's organization host URL for notifications. (or look for an alternative way to get it)
            OperationalParamsRequest params = new OperationalParamsRequest(
                pipeline.getPipelineID(), 
                pipeline.getFaultToleranceLevel(), 
                peRef.getOrganizationHostURL(), 
                finalHeartbeatConfig);
            
            String url = peRef.getOrganizationHostURL() + "/pipelineBuilder/instance/" + instanceID + "/operational-params";
            HTTPRequest req = new HTTPRequest(url, JsonUtil.toJson(params));

            LogUtil.info("[BUILDER OP PARAMS] Sending to PE {} ({}): PipelineID={}, FTLevel={}",
                    peRef.getTemplateID(), instanceID + "-" + peRef.getInstanceNumber(), pipeline.getPipelineID(), pipeline.getFaultToleranceLevel());
            HTTPResponse resp = webClient.putSync(req);
            if (resp == null || !resp.status().is2xxSuccessful()) {                
                throw new IllegalStateException("[PIPELINE BUILDER] Failed to set operational params for " + instanceID);
            }
        }
        LogUtil.info("[BUILDER OP PARAMS] Operational parameters set for all PEs in pipeline: {}", pipeline.getPipelineID());
    }

    private HeartbeatTimingConfig getHeartbeatTimingConfigs(UserDefinedHeartbeatConfig userInput, int numPEs) {
        HeartbeatTimingConfig defaults = HeartbeatTimingConfig.getDefaults();
        
        long timeout = defaults.getTimeoutMs();
        long send    = defaults.getSendIntervalMs();
        long check   = defaults.getCheckIntervalMs();
        Integer misses = defaults.getMissesThreshold(); // default 3
        
        // ---------- 1) Apply explicit user inputs ----------
        if (userInput != null) {
            if (userInput.getMissesThreshold() != null) {
                misses = Math.max(1, userInput.getMissesThreshold());
            }
            if (userInput.getTimeoutMs() != null) {
                timeout = Math.max(10_000L, userInput.getTimeoutMs());
            }
            if (userInput.getSendIntervalMs() != null) {
                send = userInput.getSendIntervalMs();
            }
            if (userInput.getCheckIntervalMs() != null) {
                check = userInput.getCheckIntervalMs();
            }
        }
    
        // ---------- 2) Derive missing values ----------
        // If user gave a timeout but NO send, keep send roughly aligned with misses:
        if (userInput != null && userInput.getTimeoutMs() != null && userInput.getSendIntervalMs() == null) {
            // keep: misses * send ≈ timeout
            send = Math.max(300L, timeout / Math.max(2, misses));
        }
    
        // If user did NOT give a check, derive as timeout/2 (your requirement)
        if (userInput == null || userInput.getCheckIntervalMs() == null) {
            check = Math.max(50L, timeout / 2);
        }
    
        // ---------- 3) Sanity/consistency ----------
        if (send < 100L)  send  = 100L;
        if (check < 50L)  check = 50L;
    
        // keep timeout consistent for dashboards/intuition: timeout >= send * misses
        long minTimeout = send * Math.max(1, misses);
        if (timeout < minTimeout) timeout = minTimeout;
    
        // ---------- 4) Grace period (no user input; computed) ----------
        // Minimal 60s + 3s per additional PE. (For N PEs: 60_000 + (N-1)*3_000)
        long grace = 60_000L + Math.max(0, (numPEs - 1)) * 3_000L;
    
        LogUtil.info("Resolved Heartbeat Timings: Timeout=[{}], Send=[{}], Check=[{}], Grace=[{}], MissesThreshold=[{}]",
                timeout, send, check, grace, misses);
    
        return new HeartbeatTimingConfig(send, check, timeout, grace, misses);
    }



    private PEInstanceResponse sendCreateSourceRequest(ProcessingElementReference pe) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/source/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateOperatorRequest(ProcessingElementReference pe, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/operator/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        requestBody.setConsumerConfigs(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateSinkRequest(ProcessingElementReference pe, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/sink/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        requestBody.setConsumerConfigs(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendPostRequest(String url, PEInstanceRequest body) {
        HTTPResponse response = webClient.postSync(new HTTPRequest(url, JsonUtil.toJson(body)));

        if (response == null || response.body() == null) {
            throw new IllegalStateException("No response received from " + url);
        }
        PEInstanceResponse peInstanceResponse = JsonUtil.fromJson(response.body(), PEInstanceResponse.class);

        if (peInstanceResponse.getTemplateID() == null ||
                peInstanceResponse.getInstanceID() == null ||
                peInstanceResponse.getTemplateID().isEmpty() ||
                peInstanceResponse.getInstanceID().isEmpty()) {
            throw new IllegalStateException("Received invalid response from " + url + ": " + response.body());
        }
        return peInstanceResponse;
    }
}