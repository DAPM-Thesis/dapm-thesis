package pipeline.processingelement;

import communication.message.Message;
import pipeline.processingelement.heartbeat.HeartbeatManager_Phase1;
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
            // If a Sink has no data consumers but IS expected to publish an upstream pulse,
            // it needs a broker URL. This is a config issue if not provided.
            // For now, we'll rely on PipelineBuilder providing valid configuration.
            // This case (Sink without data consumers but with HB) is unusual unless brokerURL is passed differently.
            LogUtil.info("[SINK WARN] {} Instance {}: Sink has no data consumers to derive brokerUrl for heartbeats.",
                         this.getClass().getSimpleName(), getInstanceId());
            // If there's no way to get a brokerUrl, HeartbeatManager_Phase1 init will fail if it needs to publish.
            // If it only subscribes, brokerUrl is still needed for the raw KafkaConsumer.
            // This situation implies a PE needs a brokerUrl even if it has no data plane Kafka components.
            // For now, this will likely fail if hbBrokerUrl remains null and HBs are configured.
        }


        if (this.internalHeartbeatTopicConfig == null) {
            LogUtil.info("[SINK WARN PH1] {} Instance {}: HeartbeatTopicSetupConfig not set. Heartbeats inactive.",
                         this.getClass().getSimpleName(), getInstanceId());
        } else if (hbBrokerUrl != null) {
            this.heartbeatManager_Phase1 = new HeartbeatManager_Phase1(
                    this,
                    hbBrokerUrl,
                    this.internalHeartbeatTopicConfig
            );
            this.heartbeatManager_Phase1.start();
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
    public boolean terminate() {
        LogUtil.info("[SINK] {} Instance {}: Terminating Sink...", this.getClass().getSimpleName(), getInstanceId());
        return super.terminate();
    }
}
