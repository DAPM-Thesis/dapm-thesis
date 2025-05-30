package pipeline.processingelement.source;

import java.util.List;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.heartbeat.HeartbeatManager_Phase1;
import pipeline.processingelement.heartbeat.HeartbeatManager_V2;
import utils.LogUtil;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O>, ProducingProcessingElement {
    private Producer dataProducer; // Channel

    public Source(Configuration configuration) { super(configuration); }

    // @Override
    // public final void publish(O data) {
    //     dataProducer.publish(data);
    // }
    @Override
    public final void publish(O data) {
        if (dataProducer == null) {
            LogUtil.info("[SRC WARN] {} Instance {}: Data producer is null. Cannot publish data: {}", this.getClass().getSimpleName(), getInstanceId(), data);
            return;
        }
        if (!isAvailable()){
            LogUtil.info("[SRC WARN] {} Instance {}: Source not available. Dropping data: {}", this.getClass().getSimpleName(), getInstanceId(), data);
            return;
        }
        dataProducer.publish(data);
    }

    @Override
    public abstract boolean start();

     protected void finalizeStartupAndStartHeartbeat() {
        if (this.getInstanceId() == null) { // Ensure instance ID is set
             LogUtil.info("[SRC ERR PH1] {} Instance {}: Instance ID is null. Cannot start heartbeat manager.", this.getClass().getSimpleName(), "UNKNOWN");
             setAvailable(false); return;
        }
        if (dataProducer == null || dataProducer.getBrokerUrl() == null) {
            LogUtil.info("[SRC ERR PH1] {} Instance {}: Data producer or broker URL null. HB manager cannot start.", this.getClass().getSimpleName(), getInstanceId());
            setAvailable(false); // Cannot effectively heartbeat if no broker info
            return;
        }
        if (this.internalHeartbeatTopicConfig == null) {
            LogUtil.info("[SRC WARN PH1] {} Instance {}: HeartbeatTopicSetupConfig not set. Heartbeats inactive.", this.getClass().getSimpleName(), getInstanceId());
            return; // Or setAvailable(false) if HBs are mandatory for a Source
        }

        if (this.reactionHandler == null) { 
            LogUtil.info("[SRC ERR V2] {} Instance {}: ReactionHandler not initialized. Cannot start HeartbeatManager_V2.", getClass().getSimpleName(), getInstanceId());
            setAvailable(false); return;
        }

        LogUtil.info("[SRC HB PH1] {} Instance {}: Finalizing startup and starting heartbeats.", this.getClass().getSimpleName(), getInstanceId());
        this.heartbeatManager = new HeartbeatManager_V2(
                this,
                dataProducer.getBrokerUrl(),
                this.internalHeartbeatTopicConfig,
                this.reactionHandler
        );
        this.heartbeatManager.start();
    }

    // @Override
    // public boolean terminate() {
    //     if (!dataProducer.stop())
    //         { return false; }
    //     boolean terminated = dataProducer.terminate();
    //     if (terminated) dataProducer = null;
    //     return terminated;
    // }

    @Override
    public boolean terminate() {
        LogUtil.info("[SRC] {} Instance {}: Terminating Source...", this.getClass().getSimpleName(), getInstanceId());
        boolean dataProducerStopped = true;
        if (dataProducer != null) {
            dataProducerStopped = dataProducer.stop();
        }
        boolean superTerminated = super.terminate(); // Stops heartbeats
        boolean dataProducerTerminated = true;
        if (dataProducer != null) {
            dataProducerTerminated = dataProducer.terminate();
            if(dataProducerTerminated) dataProducer = null;
        }
        return dataProducerStopped && superTerminated && dataProducerTerminated;
    }

    public final void registerProducer(Producer producer) {
        this.dataProducer = producer;
    }
}
