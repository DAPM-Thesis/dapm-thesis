package pipeline.processingelement.operator;

import java.util.List;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ConsumingProcessingElement;
import communication.Publisher;
import utils.Pair;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
        implements Publisher<O>, ProducingProcessingElement {
    private Producer producer;
    private List<String> downstreaminstanceIds = List.of();

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
        producer.publish(data);
    }

    @Override
    public boolean terminate() {
        if (!super.terminate()) // terminate consumers
            { return false; }
        
        if (heartbeatManager != null) heartbeatManager.stop();
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

     @Override
    public boolean start() {
        boolean ok = super.start(); // starts consumers
        if (ok) {
            // now start heartbeats for each downstream
            initHeartbeat(producer.getBrokerUrl());
            for (String downstreamId : downstreaminstanceIds) {
                if (downstreamId == null || downstreamId.isEmpty()) {
                    System.err.println("Downstream instance ID is null or empty, skipping heartbeat link creation.");
                    continue;
                }
                String sendTopic = "hb-down-" + getInstanceId() + "-to-" + downstreamId;
                String receiveTopic = "hb-up-" + downstreamId + "-to-" + getInstanceId();
                heartbeatManager.addLink(downstreamId, sendTopic, receiveTopic);
            }
            heartbeatManager.start();
        }
        return ok;
    }    

    public final void registerProducer(Producer producer) {
        this.producer = producer;
    }

    public final void setDownstreaminstanceIds(List<String> ids) {
        this.downstreaminstanceIds = List.copyOf(ids);
    }
}
