package pipeline;


import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import communication.config.ChannelConfig;
import draft_validation.ChannelReference;
import draft_validation.PipelineDraft;
import draft_validation.ProcessingElementReference;
import draft_validation.SubscriberReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import utils.DG;
import utils.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PipelineBuilder {
    private final HTTPClient webClient;
    private DG<ProcessingElementReference> DG;

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        this.webClient = webClient;
    }

    public Pipeline buildPipeline(String organizationOwnerID, PipelineDraft pipelineDraft) {
        Pipeline pipeline = new Pipeline(organizationOwnerID);
        this.DG = new DG<>();
        initializeDG(pipelineDraft.channels());
        buildPipeline(pipeline);
        return pipeline;
    }

    private void initializeDG(Set<ChannelReference> channelReferences) {
        for (ChannelReference cr : channelReferences) {
            ProcessingElementReference producer = cr.getProducer();
            for (SubscriberReference sr : cr.getSubscribers()) {
                ProcessingElementReference consumer = sr.getElement();
                DG.addEdge(producer, consumer);
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
                if (!allInputsReady(configuredInstances, pe)) {
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

    private boolean allInputsReady(Map<ProcessingElementReference, PEInstanceResponse> configured, ProcessingElementReference pe) {
        return getUpstream(pe).stream().allMatch(configured::containsKey);
    }

    private List<ChannelConfig> getChannelConfigs(ProcessingElementReference target, Map<ProcessingElementReference, PEInstanceResponse> configured) {
        return getUpstream(target).stream()
                .map(pe -> configured.get(pe).getChannelConfig())
                .collect(Collectors.toList());
    }

    private String createSource(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        PEInstanceResponse sourceResponse = sendCreateSourceRequest(pe.getOrganizationHostURL(), pe.getTemplateID());
        configuredInstances.put(pe, sourceResponse);
        return sourceResponse.getInstanceID();
    }

    private String createOperator(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ChannelConfig> channelConfigs = getChannelConfigs(pe, configuredInstances);
        PEInstanceResponse operatorResponse = sendCreateOperatorRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), channelConfigs);
        configuredInstances.put(pe, operatorResponse);
        return operatorResponse.getInstanceID();
    }

    private String createSink(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ChannelConfig> channelConfigs = getChannelConfigs(pe, configuredInstances);
        PEInstanceResponse sinkResponse = sendCreateSinkRequest(pe.getOrganizationHostURL(), pe.getTemplateID(), channelConfigs);
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

    private PEInstanceResponse sendCreateOperatorRequest(String hostURL, String templateID, List<ChannelConfig> channelConfigs) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/operator/templateID/%s",
                encodedTemplateID
        );
        String requestBody = JsonUtil.toJson(channelConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateSinkRequest(String hostURL, String templateID, List<ChannelConfig> channelConfigs) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String url = hostURL + String.format(
                "/pipelineBuilder/sink/templateID/%s",
                encodedTemplateID
        );
        String requestBody = JsonUtil.toJson(channelConfigs);
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