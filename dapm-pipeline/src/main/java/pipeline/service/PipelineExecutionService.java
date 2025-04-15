package pipeline.service;

import communication.API.HTTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pipeline.Pipeline;
import pipeline.processingelement.ProcessingElementReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

    public void start(Pipeline pipeline) {
        if(pipeline.getSources().isEmpty()) throw new IllegalArgumentException("No sources found in pipeline");
        for(ProcessingElementReference pe : pipeline.getSources()) {
            try {
                String url = "/pipelineBuilder/start";

                webClient.post(pe.organizationHostURL() + url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
