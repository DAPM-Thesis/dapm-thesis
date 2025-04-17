package pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import communication.API.HTTPClient;
import communication.API.PEInstanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;
import utils.DAG;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO: should be able to input a Json assembly file and set up the connections
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
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        currentPipeline.getProcessingElements().add(pe);
        return this;
    }

    public PipelineBuilder connect(ProcessingElementReference from, ProcessingElementReference to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        currentPipeline.getConnections().put(from, to);
        dag.addEdge(from, to);
        return this;
    }

    public PipelineBuilder configure() {
        // A PE is configuredParent if consumers and producers have been set for sink and operators
        Map<ProcessingElementReference, PEInstanceResponse> configuredParent = new HashMap<>();
        Set<ProcessingElementReference> currentLevel = findSources();

        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for (ProcessingElementReference pe : currentLevel) {
                if (configuredParent.containsKey(pe)) continue;

                // Check if all upstream connections are configuredParent yet
                boolean allInputsReady = dag.getNodes().stream()
                        .filter(parent -> dag.getNeighbors(parent).contains(pe))
                        .allMatch(configuredParent::containsKey);

                if (!allInputsReady) {
                    // Defer to next level if waiting for inputs
                    nextLevel.add(pe);
                    continue;
                }
                if(pe.processingElementType() == ProcessingElementType.SOURCE) {
                    // Set one producer and creates the source instance
                   PEInstanceResponse sourceResponse = postToSource(pe.organizationHostURL(), pe.templateID(), pe.instanceNumber());
                   // Store source instanceIDS for starting the pipeline
                   currentPipeline.getSources().put(sourceResponse.getInstanceID(), pe);
                   configuredParent.put(pe, sourceResponse);
                }

                if(pe.processingElementType() == ProcessingElementType.OPERATOR) {
                    List<String> operatorInstanceMetaDataIDS = new ArrayList<>();
                    List<ProcessingElementReference> parents = dag.getNodes().stream()
                            .filter(parent -> dag.getNeighbors(parent).contains(pe))
                            .toList();

                    // Set all consumers
                    for (ProcessingElementReference parent : parents) {
                            String broker = configuredParent.get(parent).getBroker();
                            String topic = configuredParent.get(parent).getTopic();

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
                    configuredParent.put(pe, operatorProducerResponse);

                    // Create operator instance
                    postToOperatorCreate(pe.organizationHostURL(), pe.templateID(), operatorInstanceMetaDataIDS.toArray(new String[0]));
                }

                if(pe.processingElementType() == ProcessingElementType.SINK) {
                    List<String> sinkInstanceMetaDataIDS = new ArrayList<>();
                    List<ProcessingElementReference> parents = dag.getNodes().stream()
                            .filter(parent -> dag.getNeighbors(parent).contains(pe))
                            .toList();

                    // Set all consumers
                    for (ProcessingElementReference parent : parents) {
                        String broker = configuredParent.get(parent).getBroker();
                        String topic = configuredParent.get(parent).getTopic();

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
        Set<ProcessingElementReference> allElements = dag.getNodes();
        Set<ProcessingElementReference> sources = new HashSet<>();

        for (ProcessingElementReference pe : allElements) {
            if(pe.processingElementType() == ProcessingElementType.SOURCE) {
                sources.add(pe);
            }
        }
        return sources;
    }

    private PEInstanceResponse postToSource(String hostURL, String templateID, int instanceNumber) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/source/templateID/%s/instanceNumber/%s",
                     encodedTemplateID, instanceNumber
            );

           String response = webClient.post(hostURL + url);
           return objectMapper.readValue(response, PEInstanceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PEInstanceResponse postToOperatorConsumer(String hostURL, String templateID, int instanceNumber, String broker, String topic) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String encodedBroker = URLEncoder.encode(broker, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/operator/consumer/templateID/%s/instanceNumber/%s/broker/%s/topic/%s",
                     encodedTemplateID, instanceNumber, encodedBroker, encodedTopic
            );

            String response = webClient.post(hostURL + url);
            return objectMapper.readValue(response, PEInstanceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PEInstanceResponse postToOperatorProducer(String hostURL, String templateID, int instanceNumber) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/operator/producer/templateID/%s/instanceNumber/%s",
                    encodedTemplateID, instanceNumber
            );

            String response = webClient.post(hostURL + url);
            return objectMapper.readValue(response, PEInstanceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void postToOperatorCreate(String hostURL, String templateID, String[] instanceMetaDataIDList) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);
            String joined = String.join(",", instanceMetaDataIDList);
            String encodedMetaIds = URLEncoder.encode(joined, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/operator/templateID/%s/instance/%s",
                    encodedTemplateID, encodedMetaIds
            );

            webClient.post(hostURL + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PEInstanceResponse postToSinkConsumer(String hostURL, String templateID, int instanceNumber, String broker, String topic) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String encodedBroker = URLEncoder.encode(broker, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/sink/templateID/%s/instanceNumber/%s/broker/%s/topic/%s",
                    encodedTemplateID, instanceNumber, encodedBroker, encodedTopic
            );

            String response = webClient.post(hostURL + url);
            return objectMapper.readValue(response, PEInstanceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void postToSinkCreate(String hostURL, String templateID, String[] instanceMetaDataIDList) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);
            String joined = String.join(",", instanceMetaDataIDList);
            String encodedMetaIds = URLEncoder.encode(joined, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/sink/templateID/%s/instance/%s",
                    encodedTemplateID, encodedMetaIds
            );

            webClient.post(hostURL + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}