package pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import draft_validation.ChannelReference;
import draft_validation.PipelineDraft;
import draft_validation.ProcessingElementReference;
import draft_validation.SubscriberReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import utils.DAG;
import utils.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

// TODO: one PipelineBuilder per org - figure out storing pipelines?
@Component
public class PipelineBuilder {
    private final HTTPClient webClient;
    private final ObjectMapper objectMapper;
    private DAG<ProcessingElementReference> dag;

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    public Pipeline buildPipeline(String organizationOwnerID, PipelineDraft pipelineDraft) {
        Pipeline pipeline = new Pipeline(organizationOwnerID);
        this.dag = new DAG<>();
        build(pipeline, pipelineDraft.channels());
        return pipeline;
    }

    private void build(Pipeline pipeline, Set<ChannelReference> channelReferences) {
        connect(pipeline, channelReferences);
        // A PE is configuredPublishers if producer has been set for source and operators
        Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers = new HashMap<>();
        Set<ProcessingElementReference> currentLevel = findSources();

        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for (ProcessingElementReference pe : currentLevel) {
                if (configuredPublishers.containsKey(pe)) continue;
                // Check if all upstream connections are configuredPublishers yet
                if (!allInputsReady(configuredPublishers, pe)) {
                    // Defer to next level if waiting for inputs
                    nextLevel.add(pe);
                    continue;
                }

                if (pe.isSource()) {
                    configureSource(pipeline, configuredPublishers, pe);
                } else if (pe.isOperator()) {
                    configureOperator(configuredPublishers, pe);
                } else if (pe.isSink()) {
                    configureSink(configuredPublishers, pe);
                }

                // Add all downstream nodes for the next level
                nextLevel.addAll(dag.getNeighbors(pe));
            }
            currentLevel = nextLevel;
        }
    }

    private void connect(Pipeline pipeline, Set<ChannelReference> channelReferences) {
        for (ChannelReference cr : channelReferences) {
            ProcessingElementReference producer = cr.getProducer();
            pipeline.getProcessingElements().add(producer);

            for (SubscriberReference sr : cr.getSubscribers()) {
                ProcessingElementReference consumer = sr.getElement();
                pipeline.getProcessingElements().add(consumer);
                pipeline.getConnections().put(producer, consumer);
                dag.addEdge(producer, consumer);
            }
        }
    }

    private Set<ProcessingElementReference> findSources() {
        return dag.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSource)
                .collect(Collectors.toSet());
    }

    private Set<ProcessingElementReference> findPublishers(ProcessingElementReference pe) {
        return dag.getNodes().stream()
                .filter(parent -> dag.getNeighbors(parent).contains(pe))
                .collect(Collectors.toSet());
    }

    private boolean allInputsReady(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        return dag.getNodes().stream()
                .filter(parent -> dag.getNeighbors(parent).contains(pe))
                .allMatch(configuredPublishers::containsKey);
    }

    private void configureSource(Pipeline pipeline, Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        // Set one producer and creates the source instance
        PEInstanceResponse sourceResponse = postToSource(pe.getOrganizationHostURL(), pe.getTemplateID(), pe.getInstanceNumber());

        // Store source instanceIDS for starting the pipeline
        pipeline.getSources().put(sourceResponse.getInstanceID(), pe);
        configuredPublishers.put(pe, sourceResponse);
    }

    private void configureOperator(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        List<String> operatorInstanceMetaDataIDS = new ArrayList<>();

        // Set all consumers
        for (ProcessingElementReference publisher : findPublishers(pe)) {
            String broker = configuredPublishers.get(publisher).getBroker();
            String topic = configuredPublishers.get(publisher).getTopic();

            PEInstanceResponse operatorConsumerResponse = postToOperatorConsumer(
                    pe.getOrganizationHostURL(),
                    pe.getTemplateID(),
                    pe.getInstanceNumber(),
                    broker,
                    topic
            );
            operatorInstanceMetaDataIDS.add(operatorConsumerResponse.getInstanceMetaDataID());
        }
        // Set one producer
        PEInstanceResponse operatorProducerResponse = postToOperatorProducer(pe.getOrganizationHostURL(), pe.getTemplateID(), pe.getInstanceNumber());
        operatorInstanceMetaDataIDS.add(operatorProducerResponse.getInstanceMetaDataID());
        configuredPublishers.put(pe, operatorProducerResponse);

        // Create operator instance
        postToOperatorCreate(pe.getOrganizationHostURL(), pe.getTemplateID(), operatorInstanceMetaDataIDS.toArray(new String[0]));
    }

    private void configureSink(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        List<String> sinkInstanceMetaDataIDS = new ArrayList<>();

        // Set all consumers
        for (ProcessingElementReference publisher : findPublishers(pe)) {
            String broker = configuredPublishers.get(publisher).getBroker();
            String topic = configuredPublishers.get(publisher).getTopic();

            PEInstanceResponse operatorConsumerResponse = postToSinkConsumer(
                    pe.getOrganizationHostURL(),
                    pe.getTemplateID(),
                    pe.getInstanceNumber(),
                    broker,
                    topic
            );
            sinkInstanceMetaDataIDS.add(operatorConsumerResponse.getInstanceMetaDataID());
        }
        // Create sink instance
        postToSinkCreate(pe.getOrganizationHostURL(), pe.getTemplateID(), sinkInstanceMetaDataIDS.toArray(new String[0]));
    }

    private PEInstanceResponse postToSource(String hostURL, String templateID, int instanceNumber) {
        String encodedTemplateID = JsonUtil.encode(templateID);

        String url = String.format(
                "/pipelineBuilder/source/templateID/%s/instanceNumber/%s",
                encodedTemplateID, instanceNumber
        );
        return postWithPEInstanceResponse(hostURL + url);
    }

    private PEInstanceResponse postToOperatorConsumer(String hostURL, String templateID, int instanceNumber, String broker, String topic) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String encodedTopic = JsonUtil.encode(topic);
        String encodedBroker = JsonUtil.encode(broker);

        String url = String.format(
                "/pipelineBuilder/operator/consumer/templateID/%s/instanceNumber/%s/broker/%s/topic/%s",
                encodedTemplateID, instanceNumber, encodedBroker, encodedTopic
        );
        return postWithPEInstanceResponse(hostURL + url);
    }

    private PEInstanceResponse postToOperatorProducer(String hostURL, String templateID, int instanceNumber) {
        String encodedTemplateID = JsonUtil.encode(templateID);

        String url = String.format(
                "/pipelineBuilder/operator/producer/templateID/%s/instanceNumber/%s",
                encodedTemplateID, instanceNumber
        );
        return postWithPEInstanceResponse(hostURL + url);
    }

    private void postToOperatorCreate(String hostURL, String templateID, String[] instanceMetaDataIDList) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String joined = String.join(",", instanceMetaDataIDList);
        String encodedMetaIds = JsonUtil.encode(joined);

        String url = String.format(
                "/pipelineBuilder/operator/templateID/%s/instance/%s",
                encodedTemplateID, encodedMetaIds
        );
        post(hostURL + url);
    }

    private PEInstanceResponse postToSinkConsumer(String hostURL, String templateID, int instanceNumber, String broker, String topic) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String encodedTopic = JsonUtil.encode(topic);
        String encodedBroker = JsonUtil.encode(broker);

        String url = String.format(
                "/pipelineBuilder/sink/templateID/%s/instanceNumber/%s/broker/%s/topic/%s",
                encodedTemplateID, instanceNumber, encodedBroker, encodedTopic
        );
        return postWithPEInstanceResponse(hostURL + url);
    }

    private void postToSinkCreate(String hostURL, String templateID, String[] instanceMetaDataIDList) {
        String encodedTemplateID = JsonUtil.encode(templateID);
        String joined = String.join(",", instanceMetaDataIDList);
        String encodedMetaIds = JsonUtil.encode(joined);

        String url = String.format(
                "/pipelineBuilder/sink/templateID/%s/instance/%s",
                encodedTemplateID, encodedMetaIds
        );
        post(hostURL + url);
    }

    private PEInstanceResponse postWithPEInstanceResponse(String url) {
        try {
            String response = webClient.postSync(url);
            return objectMapper.readValue(response, PEInstanceResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void post(String url) {
        try {
            webClient.postSync(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}