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
    private String organizationHostURL;
    
    public DefaultReactionHandler() {}

    @Override
    public void initialize(ProcessingElement processingElement, String pipelineID, FaultToleranceLevel level, PipelineNotificationService notificationService, String organizationHostURL) {
        this.processingElement = Objects.requireNonNull(processingElement, "Owner PE cannot be null");
        this.pipelineID = Objects.requireNonNull(pipelineID, "PipelineId cannot be null");
        this.currentFaultToleranceLevel = Objects.requireNonNull(level, "FaultToleranceLevel cannot be null");
        this.notificationService = Objects.requireNonNull(notificationService, "NotificationService cannot be null");
        this.organizationHostURL = Objects.requireNonNull(organizationHostURL, "Organization Host URL cannot be null");
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
        if (processingElement == null || !processingElement.isAvailable()) {
            LogUtil.info("[REACTION HANDLER] {} Instance {}: Owner PE null or unavailable. Ignoring liveness failure for {} peers.",
                processingElement != null ? processingElement.getClass().getSimpleName() : "N/A",
                processingElement != null ? processingElement.getInstanceId() : "N/A",
                faultContext.affectedPeerDirection());
            return;
        }

        LogUtil.info("[REACTION HANDLER] {} Instance {}: Processing liveness failure for {} peers. Silent topics: {}. Configured Level: {}",
                processingElement.getClass().getSimpleName(), processingElement.getInstanceId(),
                faultContext.affectedPeerDirection(), faultContext.silentMonitoredTopics(),
                currentFaultToleranceLevel);

        switch (currentFaultToleranceLevel) {
            case LEVEL_1_NOTIFY_ONLY:
                handleNotifyOnly(faultContext);
                break;
            // Other levels to be implemented in Phase 2+
            case LEVEL_0_IGNORE_FAULTS:
            default:
                LogUtil.info("[REACTION HANDLER] {} Owner {}: Fault detected for {} but level is IGNORE. No action.",
                        processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), faultContext.affectedPeerDirection());
                break;
        }
    }

    private void sendNotification(PipelineNotification notification) {
        if (this.organizationHostURL == null || this.organizationHostURL.isEmpty()) {
            LogUtil.info("[REACTION HANDLER ERR] {} Owner {}: Central Notification Service URL not configured. Cannot send notification.",
                processingElement.getClass().getSimpleName(), processingElement.getInstanceId());
            return;
        }
        this.notificationService.sendNotification(notification, this.organizationHostURL);
    }

    @Override
    public void processSelfReportedCriticalError(String errorMessage, Exception exceptionDetails) {
        if (processingElement == null || notificationService == null) {
             LogUtil.info("[REACTION HANDLER ERR] Cannot process self-reported error: PE or NotificationService not initialized.");
            return;
        }
         LogUtil.info("[REACTION HANDLER SELF ERR] {} Instance {}: Processing self-reported critical error: {}",
                processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), errorMessage);

        Map<String, Object> details = new HashMap<>();
        if (exceptionDetails != null) {
            details.put("exceptionClass", exceptionDetails.getClass().getName());
            details.put("exceptionMessage", exceptionDetails.getMessage());
        }
        details.put("faultToleranceLevel", currentFaultToleranceLevel.name());

        PipelineNotification notification = new PipelineNotification(
            NotificationType.PE_SELF_DETECTED_ERROR,
            this.pipelineID,
            processingElement.getInstanceId(),
            errorMessage,
            Instant.now(),
            details
        );
        sendNotification(notification);
        
        // TODO:
        // For Level 1, even self-reported errors might just notify.
        // Higher levels might trigger PE shutdown or pipeline actions.
        if (currentFaultToleranceLevel != FaultToleranceLevel.LEVEL_0_IGNORE_FAULTS &&
            currentFaultToleranceLevel != FaultToleranceLevel.LEVEL_1_NOTIFY_ONLY) {
            // For more severe levels, a self-reported error might lead to the PE stopping itself.
            // ownerPE.setAvailable(false);
            // ownerPE.terminate(); // Or a more graceful stop for data processing
        }
    }

    private void handleNotifyOnly(FaultContext faultContext) {
        String message = String.format(
            "PE %s (%s) detected heartbeat loss from its %s. Silent monitored topics: %s. All configured topics for this group: %s.",
            processingElement.getInstanceId(),
            processingElement.getClass().getSimpleName(),
            faultContext.affectedPeerDirection().name().toLowerCase().replace("_", " "),
            faultContext.silentMonitoredTopics(),
            faultContext.allConfiguredTopicsForDirection()
        );

        NotificationType type = faultContext.affectedPeerDirection() == PeerDirection.UPSTREAM_PRODUCER ?
                NotificationType.HEARTBEAT_TIMEOUT_ON_UPSTREAM_TOPIC :
                NotificationType.HEARTBEAT_TIMEOUT_ON_DOWNSTREAM_TOPIC;

        Map<String, Object> details = new HashMap<>();
        details.put("silentTopics", faultContext.silentMonitoredTopics().toString());
        details.put("allConfiguredTopicsForDirection", faultContext.allConfiguredTopicsForDirection().toString());

        PipelineNotification notification = new PipelineNotification(
            type,
            this.pipelineID,
            processingElement.getInstanceId(),
            message,
            Instant.now(),
            details
        );
        sendNotification(notification);
        LogUtil.info("[REACTION HANDLER] {} ProcessingElement {}: Notification sent for {} fault. PE continues operation.",
            processingElement.getClass().getSimpleName(), processingElement.getInstanceId(), faultContext.affectedPeerDirection());
    }
}
