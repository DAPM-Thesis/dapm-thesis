package pipeline.processingelement.operator;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ConsumingProcessingElement;
import pipeline.processingelement.heartbeat.HeartbeatManager_V2;
import communication.Publisher;
import utils.LogUtil;
import utils.Pair;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
        implements Publisher<O>, ProducingProcessingElement {
    private Producer dataProducer;

    public Operator(Configuration configuration) { super(configuration); }

    @Override
    public final void observe(Pair<Message, Integer> inputAndPortNumber) {
        AO algorithmOutput = process(inputAndPortNumber.first(), inputAndPortNumber.second());
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract AO process(Message input, int portNumber);

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

    @Override
    public final void publish(O data) {
        dataProducer.publish(data);
    }

    public final void registerProducer(Producer producer) {
        this.dataProducer = producer;
    }

    @Override
    public boolean start() {
        LogUtil.info("[OP] {} Instance {}: Starting Operator...", this.getClass().getSimpleName(), getInstanceId());
        if (!super.start()) { // Starts data consumers
            LogUtil.info("[OP ERR] {} Instance {}: super.start() (CPE) failed.", getClass().getSimpleName(), getInstanceId());
            return false;
        }

        String hbBrokerUrl = null;
        if (dataProducer != null && dataProducer.getBrokerUrl() != null) {
            hbBrokerUrl = dataProducer.getBrokerUrl();
        } else if (!getConsumers().isEmpty()) {
            hbBrokerUrl = getConsumers().values().iterator().next().getBrokerUrl();
        }

        if (hbBrokerUrl == null && this.internalHeartbeatTopicConfig != null &&
             (this.internalHeartbeatTopicConfig.getUpstreamHeartbeatPublishTopic() != null ||
              this.internalHeartbeatTopicConfig.getDownstreamHeartbeatPublishTopic() != null ||
              !this.internalHeartbeatTopicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty() ||
              !this.internalHeartbeatTopicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty() )) {
            LogUtil.info("[OP WARN] {} Instance {}: No Kafka broker URL for heartbeats, but HB config exists. HB might be non-operational.",
                         this.getClass().getSimpleName(), getInstanceId());
        }

        if (this.internalHeartbeatTopicConfig == null) {
            LogUtil.info("[OP WARN PH1] {} Instance {}: HeartbeatTopicSetupConfig not set. Heartbeats inactive.",
                         this.getClass().getSimpleName(), getInstanceId());
        } else if (hbBrokerUrl != null) {
             this.heartbeatManager = new HeartbeatManager_V2(
                    this,
                    hbBrokerUrl,
                    this.internalHeartbeatTopicConfig,
                    this.reactionHandler
            );
            this.heartbeatManager.start();
            LogUtil.info("[OP HB PH1] {} Instance {}: HeartbeatManager_Phase1 started.", getClass().getSimpleName(), getInstanceId());
        } else if (this.internalHeartbeatTopicConfig.getUpstreamHeartbeatPublishTopic()!=null || 
                   this.internalHeartbeatTopicConfig.getDownstreamHeartbeatPublishTopic()!=null ||
                   !this.internalHeartbeatTopicConfig.getUpstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty() ||
                   !this.internalHeartbeatTopicConfig.getDownstreamNeighborHeartbeatTopicsToSubscribeTo().isEmpty()){
             LogUtil.info("[OP ERR PH1] {} Instance {}: HB Config present but no broker URL. HB Manager not started.", getClass().getSimpleName(), getInstanceId());
             setAvailable(false); return false; 
        }
        
        LogUtil.info("[OP] {} Instance {}: Operator started.", this.getClass().getSimpleName(), getInstanceId());
        return isAvailable();
    }

    @Override
    public boolean stopProcessing() {
        LogUtil.info("[OP] {} Instance {}: Stopping data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(false);
        setAvailable(false); // Stop sending its own heartbeats
        boolean consumersStopped = stopDataConsumers(); // From ConsumingProcessingElement via super
        boolean producerStopped = stopDataProduction();
        return consumersStopped && producerStopped;
    }

    @Override
    public boolean resumeProcessing() {
        LogUtil.info("[OP] {} Instance {}: Resuming data processing.", getClass().getSimpleName(), getInstanceId());
        setProcessingActive(true);
        setAvailable(true); // Resume sending its own heartbeats
        boolean consumersResumed = resumeDataConsumers();
        boolean producerResumed = resumeDataProduction();
        return consumersResumed && producerResumed;
    }

    public boolean stopDataProduction() {
        if (dataProducer != null) return dataProducer.stop();
        return true;
    }

    public boolean resumeDataProduction() {
        // TODO: Implement logic to resume data production
        return true;
    }

    @Override
    public boolean terminate() {
        LogUtil.info("[OP] {} Instance {}: Terminating Operator...", this.getClass().getSimpleName(), getInstanceId());
        boolean dataProducerStopped = true;
        if (dataProducer != null) dataProducerStopped = dataProducer.stop();
        boolean superTerminated = super.terminate(); 
        boolean dataProducerTerminated = true;
        if (dataProducer != null) {
            dataProducerTerminated = dataProducer.terminate();
            if(dataProducerTerminated) dataProducer = null;
        }
        return dataProducerStopped && superTerminated && dataProducerTerminated;
    }
}
