package pipeline.processingelement.reaction;

import pipeline.notification.PipelineNotificationService;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;

public interface ReactionHandler {
    void initialize(ProcessingElement processingElement,
                    String pipelineId,
                    FaultToleranceLevel level,
                    PipelineNotificationService notificationService,
                    String organizationHostURL);

    void setFaultToleranceLevel(FaultToleranceLevel level);
    
    void processLivenessFailure(FaultContext faultContext, boolean isOptional);

    void processSelfReportedCriticalError(String errorMessage, Exception exceptionDetails);
}