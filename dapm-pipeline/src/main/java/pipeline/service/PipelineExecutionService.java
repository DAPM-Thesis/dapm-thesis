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
import repository.PipelineRepository;
import utils.LogUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class PipelineExecutionService {
    private final HTTPClient webClient;
    private final PipelineRepository pipelineRepository;

    @Autowired
    public PipelineExecutionService(HTTPClient webClient, PipelineRepository pipelineRepository) {
        this.webClient = webClient;
        this.pipelineRepository = pipelineRepository;
    }

    public void start(String pipelineID) {
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineID);
        if( pipeline == null ) {
            throw new PipelineExecutionException("Pipeline " + pipelineID + " not found");
        }
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

    public void terminate(String pipelineID) {
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineID);
        if( pipeline == null ) {
            throw new PipelineExecutionException("Pipeline " + pipelineID + " not found");
        }
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
        pipelineRepository.removePipeline(pipelineID);
    }

    public void stopPipelineForDebugging(String pipelineID) {
        LogUtil.info("[EXEC SVC] Stopping pipeline for debugging: {}", pipelineID);
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineID);
        if (pipeline == null) throw new PipelineExecutionException("Pipeline " + pipelineID + " not found for stopping.");

        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceID = entry.getKey();
            ProcessingElementReference peRef = entry.getValue();
            String url = peRef.getOrganizationHostURL() + "/pipelineExecution/stopProcessing/instance/" + instanceID;
            LogUtil.info("[EXEC SVC] Sending stopProcessing to PE {} ({}) at URL {}", peRef.getTemplateID(), instanceID, url);
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            
            if (!isSuccess(response.status())) {
                LogUtil.info("[EXEC SVC ERR] Failed to stop processing for PE {} ({}). Status: {}", peRef.getTemplateID(), instanceID, response.status());
            }
        }
         LogUtil.info("[EXEC SVC] Pipeline {} processing stopped for debugging.", pipelineID);
    }

    public void resumePipeline(String pipelineID) {
        LogUtil.info("[EXEC SVC] Resuming pipeline: {}", pipelineID);
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineID);
        if (pipeline == null) throw new PipelineExecutionException("Pipeline " + pipelineID + " not found for resuming.");

        for (Map.Entry<String, ProcessingElementReference> entry : pipeline.getProcessingElements().entrySet()) {
            String instanceID = entry.getKey();
            ProcessingElementReference peRef = entry.getValue();
            String url = peRef.getOrganizationHostURL() + "/pipelineExecution/resumeProcessing/instance/" + instanceID;
            LogUtil.info("[EXEC SVC] Sending resumeProcessing to PE {} ({}) at URL {}", peRef.getTemplateID(), instanceID, url);
            HTTPResponse response = webClient.putSync(new HTTPRequest(url));
            
            if (!isSuccess(response.status())) {
                LogUtil.info("[EXEC SVC ERR] Failed to resume processing for PE {} ({}). Status: {}", peRef.getTemplateID(), instanceID, response.status());
            }
        }
        LogUtil.info("[EXEC SVC] Pipeline {} resumed.", pipelineID);
    }

    private boolean isSuccess(HttpStatusCode status) { return status.is2xxSuccessful(); }
}
