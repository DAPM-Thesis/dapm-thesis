package pipeline;


import candidate_validation.*;
import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import communication.config.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import utils.graph.DG;
import utils.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PipelineBuilder {
    private final HTTPClient webClient;
    private DG<ProcessingElementReference, Integer> DG;

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        this.webClient = webClient;
    }

    public Pipeline buildPipeline(String organizationOwnerID, ValidatedPipeline validatedPipeline) {
        Pipeline pipeline = new Pipeline(organizationOwnerID);
        this.DG = new DG<>();
        initializeDG(validatedPipeline.getChannels());
        buildPipeline(pipeline);
        return pipeline;
    }

    private void initializeDG(Set<ChannelReference> channelReferences) {
        for (ChannelReference cr : channelReferences) {
            ProcessingElementReference producer = cr.getProducer();
            for (SubscriberReference sr : cr.getSubscribers()) {
                ProcessingElementReference consumer = sr.getElement();
                DG.addEdgeWithAttribute(producer, consumer, sr.getPortNumber());
            }
        }
    }

    private void buildPipeline(Pipeline pipeline) {
        Map<ProcessingElementReference, PEInstanceResponse> configuredInstances = new HashMap<>();
        Set<ProcessingElementReference> currentLevel = findSources();

        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for (ProcessingElementReference pe : currentLevel) {
                if (configuredInstances.containsKey(pe)) continue;
                // Check if all upstream connections are configuredInstances yet
                if (!allInputsReady(pe, configuredInstances)) {
                    // Defer to next level if waiting for inputs
                    nextLevel.add(pe);
                    continue;
                }

                if (pe.isSource()) {
                    String instanceID = createSource(configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isOperator()) {
                    String instanceID = createOperator(configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isSink()) {
                    String instanceID = createSink(configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                }

                // Add all downstream nodes for the next level
                nextLevel.addAll(DG.getNeighbors(pe));
            }
            currentLevel = nextLevel;
        }
    }

    private Set<ProcessingElementReference> getUpstream(ProcessingElementReference pe) {
        return DG.getNodes().stream()
                .filter(node -> DG.getNeighbors(node).contains(pe))
                .collect(Collectors.toSet());
    }

    private Set<ProcessingElementReference> findSources() {
        return DG.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSource)
                .collect(Collectors.toSet());
    }

    private boolean allInputsReady(ProcessingElementReference pe, Map<ProcessingElementReference, PEInstanceResponse> configured) {
        return getUpstream(pe).stream().allMatch(configured::containsKey);
    }

    private List<ConsumerConfig> getConsumerConfigs(ProcessingElementReference pe, Map<ProcessingElementReference, PEInstanceResponse> configured) {
        return getUpstream(pe).stream()
                .filter(node -> configured.get(node) != null &&
                        configured.get(node).getProducerConfig() != null &&
                        DG.getEdgeAttribute(node, pe) != null)
                .map(node -> new ConsumerConfig(
                        configured.get(node).getProducerConfig().brokerURL(),
                        configured.get(node).getProducerConfig().topic(),
                        DG.getEdgeAttribute(node, pe)))
                .collect(Collectors.toList());
    }

    private String createSource(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        PEInstanceResponse sourceResponse = sendCreateSourceRequest(pe.getOrganizationHostURL(), pe.getTemplateID());
        if (sourceResponse == null) {
            throw new IllegalStateException("No response received for source: " + pe);
        }
        configuredInstances.put(pe, sourceResponse);
        return sourceResponse.getInstanceID();
    }

    private String createOperator(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for operator: " + pe);
        }
        PEInstanceResponse operatorResponse = sendCreateOperatorRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), consumerConfigs);
        if (operatorResponse == null) {
            throw new IllegalStateException("No response received for operator: " + pe);
        }
        configuredInstances.put(pe, operatorResponse);
        return operatorResponse.getInstanceID();
    }

    private String createSink(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for sink: " + pe);
        }
        PEInstanceResponse sinkResponse = sendCreateSinkRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), consumerConfigs);
        if (sinkResponse == null) {
            throw new IllegalStateException("No response received for sink: " + pe);
        }
        configuredInstances.put(pe, sinkResponse);
        return sinkResponse.getInstanceID();
    }

    private PEInstanceResponse sendCreateSourceRequest(String hostURL, String templateID) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/source/templateID/%s",
                encodedTemplateID
        );
        return sendPostRequest(url);
    }

    private PEInstanceResponse sendCreateOperatorRequest(String hostURL, String templateID, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/operator/templateID/%s",
                encodedTemplateID
        );
        String requestBody = JsonUtil.toJson(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateSinkRequest(String hostURL, String templateID, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/sink/templateID/%s",
                encodedTemplateID
        );
        String requestBody = JsonUtil.toJson(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendPostRequest(String url) {
        return sendPostRequest(url, null);
    }

    private PEInstanceResponse sendPostRequest(String url, String body) {
        String response = (body == null)
                ? webClient.postSync(url)
                : webClient.postSync(url, body);

        return JsonUtil.fromJson(response, PEInstanceResponse.class);
    }
}