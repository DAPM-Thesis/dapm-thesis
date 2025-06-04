package pipeline.processingelement.reaction;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pipeline.notification.PipelineNotificationService;
import pipeline.notification.NotificationType;
import pipeline.notification.PipelineNotification;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import utils.LogUtil;

public class DefaultReactionHandler implements ReactionHandler {
    private ProcessingElement processingElement;
    private String pipelineID;
    private FaultToleranceLevel currentFaultToleranceLevel;
    private PipelineNotificationService notificationService;
    private String peHostOrganizationURL;
    private String pipelineOwnerOrgHostURL;
    // TODO: think of a another way to call the execution service: could a new component like PipelineManager
    //private String executionServiceURL = "http://localhost:8084"; 
    // private HTTPWebClient httpClient = new HTTPWebClient();
    
    public DefaultReactionHandler() {}

    @Override
    public void initialize(ProcessingElement processingElement, String pipelineID, FaultToleranceLevel level, PipelineNotificationService notificationService, String organizationHostURL) {
        this.processingElement = Objects.requireNonNull(processingElement, "PE cannot be null");
        this.pipelineID = Objects.requireNonNull(pipelineID, "PipelineId cannot be null");
        this.currentFaultToleranceLevel = Objects.requireNonNull(level, "FaultToleranceLevel cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "NotificationService cannot be null");
        this.peHostOrganizationURL = Objects.requireNonNull(organizationHostURL, "Organization Host URL cannot be null");
        
        LogUtil.info("[REACTION HANDLER INIT] {} Instance {}: Initialized for Pipeline {} with Level: {}",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), this.pipelineID, level);
    }
    
    @Override
    public void setFaultToleranceLevel(FaultToleranceLevel level) {
        this.currentFaultToleranceLevel = Objects.requireNonNull(level);
         LogUtil.info("[REACTION HANDLER UPDATE] {} Instance {}: Fault tolerance level updated to {}",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), level);
    }

    @Override
    public void processLivenessFailure(FaultContext faultContext) {
        LogUtil.info("[REACTION HANDLER] {} Instance {}: Processing liveness failure for {} peers. Silent topics: {}. Configured Level: {}",
                processingElement.getClass().getSimpleName(), processingElement.getInstanceId(),
                faultContext.affectedPeerDirection(), faultContext.silentMonitoredTopics(),
                currentFaultToleranceLevel);

        switch (currentFaultToleranceLevel) {
            case LEVEL_NOTIFY_ONLY:
                handleNotifyOnly(faultContext);
                break;
            case LEVEL_TERMINATE_ENTIRE_PIPELINE:
                handlePipelineTerminate(faultContext);
                break;
            case LEVEL_KEEP_RUNNING_PARTIAL_PIPELINE:
                handleKeepRunningPartialPipeline(faultContext);
                break;
            case LEVEL_RESTART_FAILED_INSTANCE:
            default:
                break;
        }
    }

    private void sendNotification(PipelineNotification notification) {
        if (this.peHostOrganizationURL == null || this.peHostOrganizationURL.isEmpty()) {
            return;
        }
        this.notificationService.sendNotification(notification, this.peHostOrganizationURL);
    }

    private void sendNotification(FaultContext faultContext) {
        String message = String.format(
            "PE %s (%s) detected heartbeat loss from its %s. Silent monitored topics: %s. All configured topics for this group: %s.",
            processingElement.getInstanceId(),
            processingElement.getClass().getSimpleName(),
            faultContext.affectedPeerDirection().name().toLowerCase().replace("_", " "),
            faultContext.silentMonitoredTopics(),
            faultContext.allConfiguredTopicsForDirection()
        );

        NotificationType notificationType = faultContext.affectedPeerDirection() == PeerDirection.UPSTREAM_PRODUCER ?
                NotificationType.HEARTBEAT_TIMEOUT_ON_UPSTREAM_TOPIC :
                NotificationType.HEARTBEAT_TIMEOUT_ON_DOWNSTREAM_TOPIC;

        Map<String, Object> details = new HashMap<>();
        details.put("silentTopics", faultContext.silentMonitoredTopics().toString());
        details.put("allConfiguredTopicsForDirection", faultContext.allConfiguredTopicsForDirection().toString());

        PipelineNotification notification = new PipelineNotification(
            notificationType,
            this.pipelineID,
            processingElement.getInstanceId(),
            message,
            Instant.now(),
            details
        );

        sendNotification(notification);
    }

    //TODO: Make the goal clear for this emthod: whether do we want this or not
    @Override
    public void processSelfReportedCriticalError(String errorMessage, Exception exceptionDetails) {
        
        // if (processingElement == null || notificationService == null) {
        //      LogUtil.info("[REACTION HANDLER ERR] Cannot process self-reported error: PE or NotificationService not initialized.");
        //     return;
        // }

        // Map<String, Object> details = new HashMap<>();
        // if (exceptionDetails != null) {
        //     details.put("exceptionClass", exceptionDetails.getClass().getName());
        //     details.put("exceptionMessage", exceptionDetails.getMessage());
        // }
        // details.put("faultToleranceLevel", currentFaultToleranceLevel.name());

        // PipelineNotification notification = new PipelineNotification(
        //     NotificationType.PE_SELF_DETECTED_ERROR,
        //     this.pipelineID,
        //     processingElement.getInstanceId(),
        //     errorMessage,
        //     Instant.now(),
        //     details
        // );
        // sendNotification(notification);
        
        // // TODO:
        // // For Level 1, even self-reported errors might just notify.
        // // Higher levels might trigger PE shutdown or pipeline actions.
        // if (currentFaultToleranceLevel != FaultToleranceLevel.LEVEL_0_IGNORE_FAULTS &&
        //     currentFaultToleranceLevel != FaultToleranceLevel.LEVEL_1_NOTIFY_ONLY) {
        //     // For more severe levels, a self-reported error might lead to the PE stopping itself.
        //     // processingElement.setAvailable(false);
        //     // processingElement.terminate(); // Or a more graceful stop for data processing
        // }
    }

    private void handleNotifyOnly(FaultContext faultContext) {
        sendNotification(faultContext);
        LogUtil.info("[REACTION HANDLER] {} ProcessingElement {}: Notification sent for {} fault. PE continues operation.",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), faultContext.affectedPeerDirection());
    }

    private void handlePipelineTerminate(FaultContext faultContext) {
        LogUtil.info("[REACTION HANDLER LVL2] {} processingElement {}: Terminating entire pipeline due to fault with {} peers.",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), faultContext.affectedPeerDirection());

        sendNotification(faultContext);

        processingElement.terminate();        
    }

    private void handleKeepRunningPartialPipeline(FaultContext faultContext) {
         LogUtil.info("[REACTION HANDLER LVL3] {} processingElement {}: Partial pipeline mode. Fault with {} peers.",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), faultContext.affectedPeerDirection());
        
        sendNotification(faultContext);

        // TODO: move this logic to a service that handles pipeline actions (ex: PipelineManager)
        // for responsibility separation
        processingElement.terminate();
    }
}
