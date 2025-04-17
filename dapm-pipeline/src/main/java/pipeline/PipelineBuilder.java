package pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.ProcessingElementReference;
import utils.DAG;
import utils.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

import static pipeline.processingelement.ProcessingElementType.*;

// TODO: should be able to input a Json assembly file and set up the connections
// TODO: one PipelineBuilder per org - figure out storing pipelines?
@Component
public class PipelineBuilder {
    private Pipeline currentPipeline;
    private final HTTPClient webClient;
    private final ObjectMapper objectMapper;
    private DAG<ProcessingElementReference> dag;

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    public PipelineBuilder createPipeline(String organizationOwnerID) {
        currentPipeline = new Pipeline(organizationOwnerID);
        this.dag = new DAG<>();
        return this;
    }

    public PipelineBuilder addProcessingElement(ProcessingElementReference pe) {
        if (pe == null) {
            throw new IllegalArgumentException("processingElement cannot be null");
        }
        currentPipeline.getProcessingElements().add(pe);
        return this;
    }

    public PipelineBuilder connect(ProcessingElementReference from, ProcessingElementReference to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to)) {
            throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline.");
        }

        currentPipeline.getConnections().put(from, to);
        dag.addEdge(from, to);
        return this;
    }

    public PipelineBuilder configure() {
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

                switch (pe.processingElementType()) {
                    case SOURCE -> configureSource(configuredPublishers, pe);
                    case OPERATOR -> configureOperator(configuredPublishers, pe);
                    case SINK -> configureSink(configuredPublishers, pe);
                    default -> throw new IllegalArgumentException("could not configure processing element");
                }
                // Add all downstream nodes for the next level
                nextLevel.addAll(dag.getNeighbors(pe));
            }
            currentLevel = nextLevel;
        }
        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }

    private Set<ProcessingElementReference> findSources() {
        return dag.getNodes()
                .stream()
                .filter(pe -> pe.processingElementType() == SOURCE)
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

    private void configureSource(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        // Set one producer and creates the source instance
        PEInstanceResponse sourceResponse = postToSource(pe.organizationHostURL(), pe.templateID(), pe.instanceNumber());

        // Store source instanceIDS for starting the pipeline
        currentPipeline.getSources().put(sourceResponse.getInstanceID(), pe);
        configuredPublishers.put(pe, sourceResponse);
    }

    private void configureOperator(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        List<String> operatorInstanceMetaDataIDS = new ArrayList<>();

        // Set all consumers
        for (ProcessingElementReference publisher : findPublishers(pe)) {
            String broker = configuredPublishers.get(publisher).getBroker();
            String topic = configuredPublishers.get(publisher).getTopic();

            PEInstanceResponse operatorConsumerResponse = postToOperatorConsumer(
                    pe.organizationHostURL(),
                    pe.templateID(),
                    pe.instanceNumber(),
                    broker,
                    topic
            );
            operatorInstanceMetaDataIDS.add(operatorConsumerResponse.getInstanceMetaDataID());
        }
        // Set one producer
        PEInstanceResponse operatorProducerResponse = postToOperatorProducer(pe.organizationHostURL(), pe.templateID(), pe.instanceNumber());
        operatorInstanceMetaDataIDS.add(operatorProducerResponse.getInstanceMetaDataID());
        configuredPublishers.put(pe, operatorProducerResponse);

        // Create operator instance
        postToOperatorCreate(pe.organizationHostURL(), pe.templateID(), operatorInstanceMetaDataIDS.toArray(new String[0]));
    }

    private void configureSink(Map<ProcessingElementReference, PEInstanceResponse> configuredPublishers, ProcessingElementReference pe) {
        List<String> sinkInstanceMetaDataIDS = new ArrayList<>();

        // Set all consumers
        for (ProcessingElementReference publisher : findPublishers(pe)) {
            String broker = configuredPublishers.get(publisher).getBroker();
            String topic = configuredPublishers.get(publisher).getTopic();

            PEInstanceResponse operatorConsumerResponse = postToSinkConsumer(
                    pe.organizationHostURL(),
                    pe.templateID(),
                    pe.instanceNumber(),
                    broker,
                    topic
            );
            sinkInstanceMetaDataIDS.add(operatorConsumerResponse.getInstanceMetaDataID());
        }
        // Create sink instance
        postToSinkCreate(pe.organizationHostURL(), pe.templateID(), sinkInstanceMetaDataIDS.toArray(new String[0]));
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