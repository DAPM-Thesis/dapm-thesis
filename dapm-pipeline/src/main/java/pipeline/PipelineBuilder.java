package pipeline;

import communication.Consumer;
import communication.HTTPClient;
import communication.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.accesscontrolled.processingelement.AccessControlledProcessingElement;
import pipeline.accesscontrolled.processingelement.ChannelConfiguration;
import pipeline.processingelement.ProcessingElementReference;
import pipeline.processingelement.ProcessingElementType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TODO: should be able to input a Json assembly file and set up the connections
@Component
public class PipelineBuilder {

    private Pipeline currentPipeline;
    private final HTTPClient webClient;

    private final Map<String, String> organizations = new HashMap<>();
    private final Map<String, String> organizationBrokers = new HashMap<>();

    @Autowired
    public PipelineBuilder(HTTPClient webClient) {
        // Organizations are hard-coded
        organizations.put("orgA", "http://localhost:8082/");
        organizationBrokers.put("orgA", "localhost:29092");
        organizations.put("orgB", "http://localhost:8083/");
        organizationBrokers.put("orgB", "localhost:29082");
        this.webClient = webClient;
    }

    public PipelineBuilder createPipeline(String organizationOwnerID) {
        currentPipeline = new Pipeline(organizationOwnerID);
        return this;
    }

    /*
    public PipelineBuilder addProcessingElement(ProcessingElementReference pe) {
        if (pe == null) { throw new IllegalArgumentException("processingElement cannot be null"); }
        currentPipeline.getProcessingElements().add(pe);
        if(pe.processingElementType() == ProcessingElementType.SOURCE) currentPipeline.getSources().add(pe);
        return this;
    }*/
    public PipelineBuilder addACPE(AccessControlledProcessingElement<?> acpe){
        nullCheck(acpe);
        currentPipeline.addACPE(acpe);
        return this;
    }
    public PipelineBuilder connect(AccessControlledProcessingElement<?> from, AccessControlledProcessingElement<?>to){
        nullCheck(from);
        nullCheck(to);

        String dataTopic = "data-" + UUID.randomUUID();
        String hbUpTopic = "hbUp-" + UUID.randomUUID();
        String hbDownTopic = "hbDown-" + UUID.randomUUID();

        // TODO: CONFIRM (should the orgId be in the Token class or ACPE)
        String brokerUrl = organizationBrokers.get(from.getToken().getOrganizationID());
        String hostFrom = organizations.get(from.getToken().getOrganizationID());
        String hostTo = organizations.get(to.getToken().getOrganizationID());

        //postToOrganization(from, connectionTopic, brokerFrom, hostFrom, true);
        //postToOrganization(to, connectionTopic, brokerFrom, hostTo, false);

        postToOrganization(from, dataTopic, brokerUrl, hostFrom, true);
        postToOrganization(from, hbDownTopic, brokerUrl, hostFrom, true);
        postToOrganization(from, hbUpTopic, brokerUrl, hostFrom, false);

        postToOrganization(to, dataTopic, brokerUrl, hostTo, false);
        postToOrganization(to, hbDownTopic, brokerUrl, hostTo, false);
        postToOrganization(to, hbUpTopic, brokerUrl, hostTo, true);

        ChannelConfiguration config = new ChannelConfiguration(dataTopic, null, hbDownTopic, brokerUrl);
        from.addChannelConfiguration(config);
        to.addChannelConfiguration(config);

        currentPipeline.addConnection(from, to);

        return this;
    }

    private static void nullCheck(AccessControlledProcessingElement<?> acpe) {
        if(acpe == null || acpe.getProcessingElement() == null){
            throw new IllegalArgumentException("AccessControlledProcessingElement or Processing Element cannot be null");
        }
    }

    /*

    // Before implementing ACPE wrapper
    public PipelineBuilder connect(ProcessingElementReference from, ProcessingElementReference to) {
        if (!currentPipeline.getProcessingElements().contains(from) || !currentPipeline.getProcessingElements().contains(to))
        { throw new IllegalArgumentException("could not connect the two processing elements; they are not in the pipeline."); }

        currentPipeline.getConnections().put(from, to);
        String connectionTopic = "Topic-" + UUID.randomUUID();

        String brokerFrom = organizationBrokers.get(from.organizationID());
        String hostFrom = organizations.get(from.organizationID());
        postToOrganization(from, connectionTopic, brokerFrom, hostFrom, true);

        String hostTo = organizations.get(to.organizationID());
        postToOrganization(to, connectionTopic, brokerFrom, hostTo, false);
        return this;
    }
    */

    public Pipeline getCurrentPipeline() {
        return currentPipeline;
    }
    /*
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

            webClient.post(hostURL + url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */
    private void postToOrganization(AccessControlledProcessingElement<?> acpe, String topic,String broker, String hostURL, boolean isPublisher){
        try{
            String organizationID = URLEncoder.encode(acpe.getToken().getOrganizationID(), StandardCharsets.UTF_8);
            String processElementID = URLEncoder.encode(String.valueOf(acpe.getProcessingElement().getID()), StandardCharsets.UTF_8);
            String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8);
            String encodedBroker = URLEncoder.encode(broker, StandardCharsets.UTF_8);
            String role = isPublisher ? "publisher" : "subscriber";

            String url = String.format(
                    "/%s/%s/%s/broker/%s/topic/%s",
                    organizationID, processElementID, role, encodedBroker, encodedTopic
            );

            webClient.post(hostURL + url);
        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }
}
