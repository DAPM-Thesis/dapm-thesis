package pipeline.processingelement.source;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;
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
        if (dataProducer == null || !isAvailable()) {
            LogUtil.info("[SOURCE WARN] {} [{}]: {}. Dropping: {}", this.getClass().getSimpleName(), getInstanceId(), dataProducer == null ? "Data producer is null" : "Source not available", data);
            return;
        }
        dataProducer.publish(data);
    }

    @Override
    public abstract boolean start();

    @Override
    public boolean stopProcessing() {
        LogUtil.info("[SRC] {} Instance {}: Stopping data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(false); 
        setAvailable(false); // Stop sending its own heartbeats
        return stopDataProduction();
    }

    @Override
    public boolean resumeProcessing() {
        LogUtil.info("[SRC] {} Instance {}: Resuming data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(true);
        setAvailable(true); // Resume sending its own heartbeats
        return resumeDataProduction();
    }

    public boolean stopDataProduction() {
        if (dataProducer != null) return dataProducer.stop();
        return true;
    }

    public boolean resumeDataProduction() {
        // TODO: Figure out how to resume data production
        return true;
    }

    protected void finalizeStartupAndStartHeartbeat() {
        if (this.getInstanceId() == null) { // Ensure instance ID is set
             LogUtil.info("[SOURCE ERR] {} Instance {}: Instance ID is null. Cannot start heartbeat manager.", this.getClass().getSimpleName(), "UNKNOWN");
             setAvailable(false); return;
        }
        if (dataProducer == null || dataProducer.getBrokerUrl() == null) {
            LogUtil.info("[SOURCE ERR] {} Instance {}: Data producer or broker URL null. HB manager cannot start.", this.getClass().getSimpleName(), getInstanceId());
            setAvailable(false); // Cannot effectively heartbeat if no broker info
            return;
        }
        if (this.internalHeartbeatTopicConfig == null) {
            LogUtil.info("[SOURCE WARN] {} Instance {}: HeartbeatTopicSetupConfig not set. Heartbeats inactive.", this.getClass().getSimpleName(), getInstanceId());
            return; // Or setAvailable(false) if HBs are mandatory for a Source
        }

        if (this.reactionHandler == null) { 
            LogUtil.info("[SOURCE ERR] {} Instance {}: ReactionHandler not initialized. Cannot start HeartbeatManager_V2.", getClass().getSimpleName(), getInstanceId());
            setAvailable(false); return;
        }

        LogUtil.info("[SOURCE HB] {} Instance {}: Finalizing startup and starting heartbeats.", this.getClass().getSimpleName(), getInstanceId());
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
        LogUtil.info("[SOURCE] {} Instance {}: Terminating Source...", this.getClass().getSimpleName(), getInstanceId());
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
