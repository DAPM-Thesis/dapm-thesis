package pipeline.processingelement;

import pipeline.processingelement.heartbeat.HeartbeatManager_Phase1;
import pipeline.processingelement.heartbeat.HeartbeatTopicConfig; // Correct DTO
import utils.LogUtil;

public abstract class ProcessingElement {
    protected final Configuration configuration;
    private volatile boolean available = true;
    private String instanceID;

    protected HeartbeatTopicConfig internalHeartbeatTopicConfig;
    protected HeartbeatManager_Phase1 heartbeatManager_Phase1;

    public ProcessingElement(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setInstanceId(String instanceID) {
        this.instanceID = instanceID;
        LogUtil.info("[PE SETUP] {} Instance {} ID set.", this.getClass().getSimpleName(), this.instanceID);
    }

    public String getInstanceId() {
        if (instanceID == null) {
            LogUtil.info("[PE WARN] {} Instance ID accessed before being set.", this.getClass().getSimpleName());
        }
        return instanceID;
    }

    /**
     * Called by PipelineBuilderController to provide the PE with its heartbeat topic configuration.
     */
    public void configureHeartbeatTopics(HeartbeatTopicConfig config) {
        this.internalHeartbeatTopicConfig = config;
    }

    public abstract boolean start();

    public boolean terminate() {
        LogUtil.info("[PE TERMINATE] {} Instance {}: Base terminate called.", this.getClass().getSimpleName(), getInstanceId());
        setAvailable(false);
        if (heartbeatManager_Phase1 != null) {
            LogUtil.info("[PE TERMINATE] {} Instance {}: Stopping HeartbeatManager_Phase1.", this.getClass().getSimpleName(), getInstanceId());
            heartbeatManager_Phase1.stop();
            heartbeatManager_Phase1 = null;
        }
        return true;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        if (this.available && !available) {
            LogUtil.info("[PE STATE] {} Instance {}: Available: FALSE", this.getClass().getSimpleName(), this.getInstanceId());
        } else if (!this.available && available) {
            LogUtil.info("[PE STATE] {} Instance {}: Available: TRUE", this.getClass().getSimpleName(), this.getInstanceId());
        }
        this.available = available;
    }
}