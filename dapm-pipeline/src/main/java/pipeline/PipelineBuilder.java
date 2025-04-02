package pipeline;

import org.springframework.web.reactive.function.client.WebClient;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PipelineBuilder {
    private Pipeline currentPipeline;
    private WebClient webClient;

    public PipelineBuilder() {
        webClient = WebClient.builder().baseUrl("").build();
    }

    public PipelineBuilder createPipeline(String organizationOwnerID) {
        currentPipeline = new Pipeline(organizationOwnerID);
        return this;
    }

    public PipelineBuilder addProcessingElement(ProcessingElementReference pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        currentPipeline.getProcessingElements().add(pe);
        if(pe.processingElementType() == ProcessingElementType.SOURCE) {
            currentPipeline.getSources().add(pe);
        }
        return this;
    }

    public PipelineBuilder connect(ProcessingElementReference from, ProcessingElementReference to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        currentPipeline.getConnections().put(from, to);
        String connectionTopic = UUID.randomUUID().toString();
        String brokerFromPipelineOwner = ""; // hard coded for now
        String externalBroker = ""; // hard coded for now
        boolean pipelineOwnerOwnsPublisher = from.organizationID().equals(currentPipeline.getOrganizationOwnerID());
        String broker = pipelineOwnerOwnsPublisher ? brokerFromPipelineOwner : externalBroker;

        postToOrganization(from, connectionTopic, broker);
        postToOrganization(to, connectionTopic, broker);
        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }

    private void postToOrganization(ProcessingElementReference ref, String topic, String broker) {
        try {
            String organizationID = URLEncoder.encode(ref.organizationID(), StandardCharsets.UTF_8);
            String processElementID = URLEncoder.encode(String.valueOf(ref.processElementID()), StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String url = "/" + organizationID + "/" + processElementID + "/broker/" + broker + "/topic/" + encodedTopic;

            webClient.post().uri(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void start() {
        for(ProcessingElementReference pe : currentPipeline.getSources()) {
            String organizationID = URLEncoder.encode(pe.organizationID(), StandardCharsets.UTF_8);
            String processElementID = URLEncoder.encode(String.valueOf(pe.processElementID()), StandardCharsets.UTF_8);
            String url = "/" + organizationID + "/"
                    + processElementID + "/start";
            
            webClient.post().uri(url);
        }
    }

}
