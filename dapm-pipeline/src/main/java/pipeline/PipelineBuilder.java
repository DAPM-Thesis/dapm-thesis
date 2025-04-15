package pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import communication.API.HTTPClient;
import communication.API.SourceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

// TODO: should be able to input a Json assembly file and set up the connections
@Component
public class PipelineBuilder {

    private Pipeline currentPipeline;
    private final HTTPClient webClient;
    private ObjectMapper objectMapper;

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    public PipelineBuilder createPipeline(String organizationOwnerID) {
        currentPipeline = new Pipeline(organizationOwnerID);
        return this;
    }

    public PipelineBuilder addProcessingElement(ProcessingElementReference pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        currentPipeline.getProcessingElements().add(pe);
        if(pe.processingElementType() == ProcessingElementType.SOURCE) currentPipeline.getSources().add(pe);
        return this;
    }

    public PipelineBuilder connect(ProcessingElementReference from, ProcessingElementReference to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        Collection<String> instanceIDS = new ArrayList<>();
        currentPipeline.getConnections().put(from, to);
        SourceResponse response = postToSource(from.organizationHostURL(), from.templateID(), from.instanceNumber());
        instanceIDS.add(response.instanceID());

        postToOperator(to.organizationHostURL(), to.templateID(), to.instanceNumber(), response.topic(), response.brokerURL(), true);

        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }

    private SourceResponse postToSource(String hostURL, String templateID, int instanceNumber) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);

            String url = String.format(
                    "/pipelineBuilder/source/templateID/%s/instanceNumber/%s",
                     encodedTemplateID, instanceNumber
            );

           String response = webClient.post(hostURL + url);
           return objectMapper.readValue(response, SourceResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void postToOperator(String hostURL, String templateID, int instanceNumber, String broker, String topic, boolean setProducer) {
        try {
            String encodedTemplateID = URLEncoder.encode(templateID, StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String encodedBroker = URLEncoder.encode(broker, StandardCharsets.UTF_8);
            String role = setProducer ? "producer" : "consumer";

            String url = String.format(
                    "/pipelineBuilder/operator/%s/templateID/%s/instanceNumber/%s/broker/%s/topic/%s",
                     role, encodedTemplateID, instanceNumber, encodedBroker, encodedTopic
            );

            webClient.post(hostURL + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
