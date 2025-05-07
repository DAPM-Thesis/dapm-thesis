package pipeline;


import candidate_validation.*;
import communication.API.*;
import communication.API.request.HTTPRequest;
import communication.API.request.PEInstanceRequest;
import communication.API.response.HTTPResponse;
import communication.API.response.PEInstanceResponse;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
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
                .filter(node -> {
                    PEInstanceResponse response = configured.get(node);
                    ProducerConfig producerConfig = response != null ? response.getProducerConfig() : null;
                    boolean hasEdgeAttribute = DG.getEdgeAttribute(node, pe) != null;
                    return producerConfig != null
                            && hasEdgeAttribute
                            && producerConfig.brokerURL() != null && !producerConfig.brokerURL().isEmpty()
                            && producerConfig.topic() != null && !producerConfig.topic().isEmpty();
                })
                .map(node -> new ConsumerConfig(
                        configured.get(node).getProducerConfig().brokerURL(),
                        configured.get(node).getProducerConfig().topic(),
                        DG.getEdgeAttribute(node, pe)))
                .collect(Collectors.toList());
    }

    private String createSource(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        PEInstanceResponse sourceResponse = sendCreateSourceRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), pe.getConfiguration());
        configuredInstances.put(pe, sourceResponse);
        return sourceResponse.getInstanceID();
    }

    private String createOperator(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerData = getConsumerConfigs(pe, configuredInstances);
        if (consumerData == null || consumerData.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for operator: " + pe);
        }
        PEInstanceResponse operatorResponse = sendCreateOperatorRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), consumerData, pe.getConfiguration());
        configuredInstances.put(pe, operatorResponse);
        return operatorResponse.getInstanceID();
    }

    private String createSink(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerData = getConsumerConfigs(pe, configuredInstances);
        if (consumerData == null || consumerData.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for sink: " + pe);
        }
        PEInstanceResponse sinkResponse = sendCreateSinkRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), consumerData, pe.getConfiguration());
        configuredInstances.put(pe, sinkResponse);
        return sinkResponse.getInstanceID();
    }

    private PEInstanceResponse sendCreateSourceRequest(String hostURL, String templateID, Map<String, Object> configuration) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/source/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(configuration);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateOperatorRequest(String hostURL, String templateID, List<ConsumerConfig> consumerData, Map<String, Object> configuration) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/operator/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(configuration);
        requestBody.setConsumerConfigs(consumerData);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateSinkRequest(String hostURL, String templateID, List<ConsumerConfig> consumerData, Map<String, Object> configuration) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/sink/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(configuration);
        requestBody.setConsumerConfigs(consumerData);
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