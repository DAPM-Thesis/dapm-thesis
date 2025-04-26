package pipeline.service;

import communication.API.HTTPClient;
import draft_validation.ProcessingElementReference;
import exceptions.PipelineExecutionException;
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
            String instanceId = entry.getKey();
            String url = entry.getValue().getOrganizationHostURL() +
                    "/pipelineExecution/start/instance/" + instanceId;
            String response = webClient.putSync(url);

            if (isError(response)) {
                throw new PipelineExecutionException("Failed to start PE " + instanceId);
            }
        }
    }

    public void stop(Pipeline pipeline) {
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceId = entry.getKey();
            String url = entry.getValue().getOrganizationHostURL() +
                    "/pipelineExecution/stop/instance/" + instanceId;
            String response = webClient.putSync(url);

            if (isError(response)) {
                throw new PipelineExecutionException("Failed to stop PE " + instanceId);
            }
        }
    }

    public void terminate(Pipeline pipeline) {
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceId = entry.getKey();
            String url = entry.getValue().getOrganizationHostURL() +
                    "/pipelineExecution/terminate/instance/" + instanceId;
            String response = webClient.putSync(url);

            if (isError(response)) {
                throw new PipelineExecutionException("Failed to terminate PE " + instanceId);
            }
        }
    }

    private boolean isError(String response) {
        return response == null;
    }
}
