package pipeline.processingelement;

import pipeline.notification.PipelineNotificationService;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.HeartbeatManager_V2;
import pipeline.processingelement.heartbeat.HeartbeatTopicConfig;
import pipeline.processingelement.reaction.ReactionHandler;
import pipeline.processingelement.reaction.DefaultReactionHandler;
import utils.LogUtil;

public abstract class ProcessingElement {
    protected final Configuration configuration;
    private volatile boolean available = true;
    private String instanceID;
    private String pipelineID;

    protected HeartbeatTopicConfig internalHeartbeatTopicConfig;
    protected HeartbeatManager_V2 heartbeatManager;

    protected ReactionHandler reactionHandler;
    protected FaultToleranceLevel pipelineFaultToleranceLevel = FaultToleranceLevel.LEVEL_1_NOTIFY_ONLY;
    protected PipelineNotificationService pipelineNotificationService;
    protected String organizationHostURL;

    public ProcessingElement(Configuration configuration) {
        this.configuration = configuration;
    }   

    /**
     * Called by PipelineBuilderController to provide the PE with its heartbeat topic configuration.
     */
    public void configureHeartbeatTopics(HeartbeatTopicConfig config) {
        this.internalHeartbeatTopicConfig = config;
    }

     public void setOperationalParameters(String pipelineID, FaultToleranceLevel faultToleranceLevel, PipelineNotificationService notificationService, String organizationHostURL) {
        this.pipelineID = pipelineID;
        this.pipelineFaultToleranceLevel = faultToleranceLevel;
        this.pipelineNotificationService = notificationService;
        this.organizationHostURL = organizationHostURL;

        this.reactionHandler = new DefaultReactionHandler();
        this.reactionHandler.initialize(this, this.pipelineID, this.pipelineFaultToleranceLevel, this.pipelineNotificationService, this.organizationHostURL);
    }

    public abstract boolean start();

    public boolean terminate() {
        LogUtil.info("[PE TERMINATE] {} Instance {}: Base terminate called.", this.getClass().getSimpleName(), getInstanceId());
        setAvailable(false);
        if (heartbeatManager != null) {
            LogUtil.info("[PE TERMINATE] {} Instance {}: Stopping HeartbeatManager.", this.getClass().getSimpleName(), getInstanceId());
            heartbeatManager.stop();
            heartbeatManager = null;
        }
        return true;
    }

    // Getters and Setters
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public void setInstanceId(String instanceID) { this.instanceID = instanceID; }
    public String getInstanceId() { return instanceID; }

    public void setPipelineId(String pipelineID) { this.pipelineID = pipelineID; }
    public String getPipelineId() { return pipelineID; }


    // TODO: IS THE RIGHT PLACE FOR THIS?
    public void selfReportCriticalError(String message, Exception ex) {
        LogUtil.error(ex, "[PE SELF CRITICAL ERR] {} Instance {}: {}", getClass().getSimpleName(), getInstanceId(), message);
        if (this.reactionHandler != null) {
            this.reactionHandler.processSelfReportedCriticalError(message, ex);
        }
        // Depending on the fault tolerance level, the reaction handler might or might not stop the PE.
        // If it doesn't, and the error is truly critical, the PE might need to force its own shutdown.
        // For now, just notifying. Consider adding:
        // this.setAvailable(false);
        // if (this.pipelineFaultToleranceLevel != FaultToleranceLevel.LEVEL_1_NOTIFY_ONLY &&
        //     this.pipelineFaultToleranceLevel != FaultToleranceLevel.LEVEL_0_IGNORE_FAULTS) {
        //     this.terminate(); // Or a more graceful stop
        // }
    }
}