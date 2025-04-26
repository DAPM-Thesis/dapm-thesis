package pipeline.service;

import communication.API.HTTPClient;
import draft_validation.ProcessingElementReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pipeline.Pipeline;

import java.util.Map;

@Service
public class PipelineExecutionService {
    private final HTTPClient webClient;

    @Autowired
    public PipelineExecutionService(HTTPClient webClient) {
        this.webClient = webClient;
    }

    public void start(Pipeline pipeline) {
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
                String url = entry.getValue().getOrganizationHostURL() +
                        "/pipelineBuilder/start/instance/" + entry.getKey();
                webClient.putSync(url);
        }
    }
}
