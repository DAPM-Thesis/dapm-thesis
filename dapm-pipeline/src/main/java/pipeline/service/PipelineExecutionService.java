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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            if (!isSuccess(response.status())) {
                throw new PipelineExecutionException("Failed to start PE " + instanceId);
            }
        }
    }

    public void terminate(Pipeline pipeline) {
        Set<ProcessingElementReference> currentLevel = pipeline.getSinks();
        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for( ProcessingElementReference pe : currentLevel ) {
                String instanceID = pipeline.getInstanceID(pe);
                String url = pe.getOrganizationHostURL() + "/pipelineExecution/terminate/instance/" + instanceID;
                HTTPResponse response = webClient.putSync(new HTTPRequest(url));
                if (!isSuccess(response.status())) {
                    throw new PipelineExecutionException("Failed to terminate PE " + instanceID);
                }
                nextLevel.addAll(pipeline.getDirectedGraph().getUpstream(pe));
            }
            currentLevel = nextLevel;
        }
    }

    private boolean isSuccess(HttpStatusCode status) {
        return status.is2xxSuccessful();
    }
}
