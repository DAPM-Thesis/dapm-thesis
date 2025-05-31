package pipeline.processingelement;

import communication.message.Message;
import pipeline.processingelement.heartbeat.HeartbeatManager_V2;
import utils.LogUtil;
import utils.Pair;


public abstract class Sink extends ConsumingProcessingElement {

    public Sink(Configuration configuration) { super(configuration); }

    @Override
    public abstract void observe(Pair<Message, Integer> inputAndPortNumber);

    @Override
    public boolean start() {
        LogUtil.info("[SINK] {} Instance {}: Starting Sink...", this.getClass().getSimpleName(), getInstanceId());
        if (!super.start()) { // Starts data consumers
            LogUtil.info("[SINK ERR] {} Instance {}: super.start() (CPE) failed.", getClass().getSimpleName(), getInstanceId());
            return false;
        }

        String hbBrokerUrl = null;
        if (!getConsumers().isEmpty()) {
            hbBrokerUrl = getConsumers().values().iterator().next().getBrokerUrl();
        } else if (this.internalHeartbeatTopicConfig != null && this.internalHeartbeatTopicConfig.getUpstreamHeartbeatPublishTopic() != null) {            
            LogUtil.info("[SINK WARN] {} Instance {}: Sink has no data consumers to derive brokerUrl for heartbeats.",
                         this.getClass().getSimpleName(), getInstanceId());
        }


        if (this.internalHeartbeatTopicConfig == null) {
            LogUtil.info("[SINK WARN PH1] {} Instance {}: HeartbeatTopicSetupConfig not set. Heartbeats inactive.",
                         this.getClass().getSimpleName(), getInstanceId());
        } else if (hbBrokerUrl != null) {
            this.heartbeatManager = new HeartbeatManager_V2(
                    this,
                    hbBrokerUrl,
                    this.internalHeartbeatTopicConfig,
                    this.reactionHandler
            );
            this.heartbeatManager.start();
            LogUtil.info("[SINK HB PH1] {} Instance {}: HeartbeatManager_Phase1 started.", getClass().getSimpleName(), getInstanceId());
        } else if (this.internalHeartbeatTopicConfig.getUpstreamHeartbeatPublishTopic()!=null ||
                   !this.internalHeartbeatTopicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty() ||
                   !this.internalHeartbeatTopicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty()){
            LogUtil.info("[SINK ERR PH1] {} Instance {}: HB Config present but no broker URL. HB Manager not started.", getClass().getSimpleName(), getInstanceId());
            setAvailable(false);
            return false; 
        }
        
        LogUtil.info("[SINK] {} Instance {}: Sink started.", this.getClass().getSimpleName(), getInstanceId());
        return isAvailable();
    }

    @Override
    public boolean stopProcessing() {
        LogUtil.info("[SINK] {} Instance {}: Stopping data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(false);
        setAvailable(false); // Stop sending its own heartbeats
        return stopDataConsumers();
    }
    
    @Override
    public boolean resumeProcessing() {
        LogUtil.info("[SINK] {} Instance {}: Resuming data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(true);
        setAvailable(true); // Resume sending its own heartbeats
        return resumeDataConsumers();
    }

    @Override
    public boolean terminate() {
        LogUtil.info("[SINK] {} Instance {}: Terminating Sink...", this.getClass().getSimpleName(), getInstanceId());
        return super.terminate();
    }
}
