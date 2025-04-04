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

    // Hardcoded for now
    private final String organizationAHost = "http://localhost:8082/"; // orgA
    private final String organizationBHost = "http://localhost:8083/"; // orgB
    private final String organizationABroker = "localhost:9092"; // broker in orgA
    private final String organizationBBroker = "localhost:9082"; // broker in orgB
    private int topicCount = 0;

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
        String connectionTopic = "Topic" + topicCount;

        String broker;
        String hostURL;
        // This is hardcoded too
        if(from.processingElementType() == ProcessingElementType.SOURCE) {
            hostURL = organizationAHost;
            broker = organizationABroker;
            postToOrganization(from, connectionTopic, broker, hostURL, true);
        }
        else if(from.processingElementType() == ProcessingElementType.OPERATOR) {
            hostURL = organizationBHost;
            broker = organizationBBroker;
            postToOrganization(from, connectionTopic, broker, hostURL, true);
        }

         if(to.processingElementType() == ProcessingElementType.OPERATOR) {
            hostURL = organizationBHost;
            broker = organizationABroker;
            postToOrganization(to, connectionTopic, broker, hostURL,false);
        }
         else if(to.processingElementType() == ProcessingElementType.SINK) {
             hostURL = organizationAHost;
             broker = organizationBBroker;
             postToOrganization(to, connectionTopic, broker, hostURL,false);
         }
         topicCount++;
        return this;
    }

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }

    private void postToOrganization(ProcessingElementReference ref, String topic, String broker, String hostURL, boolean isPublisher) {
        try {
            String organizationID = URLEncoder.encode(ref.organizationID(), StandardCharsets.UTF_8);
            String processElementID = URLEncoder.encode(String.valueOf(ref.processElementID()), StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String encodedBroker = URLEncoder.encode(broker, StandardCharsets.UTF_8);

            String publisherOrSubscriber = isPublisher ? "publisher" : "subscriber";
            String url = organizationID + "/" + processElementID + "/"
                            + publisherOrSubscriber + "/broker/"
                            + encodedBroker + "/topic/" + encodedTopic;

            webClient = WebClient.builder().baseUrl(hostURL).build();
            webClient.post().uri(url)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        if(currentPipeline.getSources().isEmpty()) throw new IllegalArgumentException("No sources found in pipeline");
        for(ProcessingElementReference pe : currentPipeline.getSources()) {
            try {
                String organizationID = URLEncoder.encode(pe.organizationID(), StandardCharsets.UTF_8);
                String processElementID = URLEncoder.encode(String.valueOf(pe.processElementID()), StandardCharsets.UTF_8);
                String url = "/" + organizationID + "/"
                        + processElementID + "/start";

                webClient = WebClient.builder().baseUrl(organizationAHost).build(); // orgA has sources only at the moment
                webClient.post().uri(url)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
