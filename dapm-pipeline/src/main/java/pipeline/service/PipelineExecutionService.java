package pipeline.service;

import communication.API.HTTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pipeline.Pipeline;
import pipeline.processingelement.ProcessingElementReference;

import java.util.Map;

@Service
public class PipelineExecutionService {
    private final HTTPClient webClient;

    @Autowired
    public PipelineExecutionService(HTTPClient webClient) {
        this.webClient = webClient;
    }

    public void start(Pipeline pipeline) {
        if (pipeline.getSources().isEmpty()) throw new IllegalArgumentException("No sources found in pipeline");
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getSources().entrySet()) {
            try {
                String url = "/pipelineBuilder/start/instance/" + entry.getKey();

                webClient.postSync(entry.getValue().organizationHostURL() + url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
