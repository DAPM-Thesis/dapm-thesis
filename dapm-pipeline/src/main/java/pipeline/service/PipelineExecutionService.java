package pipeline.service;

import communication.API.HTTPClient;
import communication.API.response.HTTPResponse;
import exceptions.PipelineExecutionException;
import candidate_validation.ProcessingElementReference;
import communication.API.request.HTTPRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import pipeline.Pipeline;
import utils.LogUtil;

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
            LogUtil.info("Starting... PE " + entry.getValue().getTemplateID());
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            if (!isSuccess(response.status())) {
                throw new PipelineExecutionException("Failed to start PE " + instanceId);
            }
            LogUtil.info("Started pipeline successfully.");
        }
    }

    public void pause(Pipeline pipeline) {
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceId = entry.getKey();
            String url = entry.getValue().getOrganizationHostURL() +
                    "/pipelineExecution/pause/instance/" + instanceId;
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            if (!isSuccess(response.status())) {
                throw new PipelineExecutionException("Failed to stop PE " + instanceId);
            }
        }
    }

    public void terminate(Pipeline pipeline) {
        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceId = entry.getKey();
            String url = entry.getValue().getOrganizationHostURL() +
                    "/pipelineExecution/terminate/instance/" + instanceId;
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            if (!isSuccess(response.status())) {
                throw new PipelineExecutionException("Failed to terminate PE " + instanceId);
            }
        }
    }

    private boolean isSuccess(HttpStatusCode status) {
        return status.is2xxSuccessful();
    }
}
