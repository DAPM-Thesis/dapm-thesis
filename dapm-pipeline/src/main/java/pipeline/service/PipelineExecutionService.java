package pipeline.service;

import communication.HTTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pipeline.Pipeline;
import pipeline.accesscontrolled.processingelement.AccessControlledProcessingElement;
import pipeline.accesscontrolled.processingelement.ProcessingElementToken;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.ProcessingElementReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PipelineExecutionService {
    private final HTTPClient webClient;

    private final Map<String, String> organizations = new HashMap<>();

    @Autowired
    public PipelineExecutionService(HTTPClient webClient) {
        // Organizations are hard-coded
        organizations.put("orgA", "http://localhost:8082/");
        organizations.put("orgB", "http://localhost:8083/");
        this.webClient = webClient;
    }

    /*
    public void start(Pipeline pipeline) {
        if(pipeline.getSources().isEmpty()) throw new IllegalArgumentException("No sources found in pipeline");
        for(ProcessingElementReference pe : pipeline.getSources()) {
            try {
                String organizationID = URLEncoder.encode(pe.organizationID(), StandardCharsets.UTF_8);
                String processElementID = URLEncoder.encode(String.valueOf(pe.processElementID()), StandardCharsets.UTF_8);
                String sourceHost = organizations.get(pe.organizationID());

                String url = String.format(
                        "/%s/%s/start",
                        organizationID, processElementID
                );

                webClient.post(sourceHost + url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
     */
    public void start(Pipeline pipeline) {
        Set<AccessControlledProcessingElement<?>> sources = pipeline.getSources();
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("No sources found in pipeline");
        }

        for (AccessControlledProcessingElement<?> acpe : sources) {
            try {
                // Retrieve organization and process element ID
                ProcessingElementToken token = acpe.getToken();
                String organizationID = URLEncoder.encode(token.getOrganizationID(), StandardCharsets.UTF_8);
                ProcessingElement pe = acpe.getProcessingElement();
                String processElementID = URLEncoder.encode(String.valueOf(pe.getID()), StandardCharsets.UTF_8);

                String sourceHost = organizations.get(token.getOrganizationID());
                if (sourceHost == null) {
                    throw new IllegalStateException("No host configured for organization: " + token.getOrganizationID());
                }

                // Build the URL. The assumed URL pattern is: /{orgID}/{processElementID}/start
                String url = String.format("/%s/%s/start", organizationID, processElementID);

                webClient.post(sourceHost + url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
