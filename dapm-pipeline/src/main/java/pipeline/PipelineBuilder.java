package pipeline;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PipelineBuilder {
    private Pipeline currentPipeline;
    private WebClient webClient;

    private final Map<String, String> organizations = new HashMap<>();
    private final Map<String, String> organizationBrokers = new HashMap<>();

    public PipelineBuilder() {
        // Organizations are hard-coded
        organizations.put("orgA", "http://localhost:8082/");
        organizationBrokers.put("orgA", "localhost:29092");
        organizations.put("orgB", "http://localhost:8083/");
        organizationBrokers.put("orgB", "localhost:29082");
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

        currentPipeline.getConnections().put(from, to);
        String connectionTopic = "Topic-" + UUID.randomUUID();

        String brokerFrom = organizationBrokers.get(from.organizationID());
        String hostFrom = organizations.get(from.organizationID());
        postToOrganization(from, connectionTopic, brokerFrom, hostFrom, true);

        String brokerTo = organizationBrokers.get(to.organizationID());
        String hostTo = organizations.get(to.organizationID());
        postToOrganization(to, connectionTopic, brokerTo, hostTo, false);
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
            String role = isPublisher ? "publisher" : "subscriber";

            String url = String.format(
                    "%s/%s/%s/broker/%s/topic/%s",
                    organizationID, processElementID, role, encodedBroker, encodedTopic
            );

            webClient = WebClient.builder().baseUrl(hostURL).build();
            webClient.post().uri(url)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
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
                String sourceHost = organizations.get(pe.organizationID());

                String url = String.format(
                        "/%s/%s/start",
                        organizationID, processElementID
                );

                webClient = WebClient.builder().baseUrl(sourceHost).build();
                webClient.post().uri(url)
                        .retrieve()
                        .bodyToMono(Void.class)
                        .subscribe();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
