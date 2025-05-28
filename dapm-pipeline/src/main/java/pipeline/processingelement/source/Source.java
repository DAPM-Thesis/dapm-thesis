package pipeline.processingelement.source;

import java.util.List;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O>, ProducingProcessingElement {
    private Producer producer; // Channel
    private List<String> downstreaminstanceIds = List.of();

    public Source(Configuration configuration) { super(configuration); }

    @Override
    public final void publish(O data) {
        producer.publish(data);
    }

    @Override
    public abstract boolean start();

    @Override
    public boolean terminate() {
        if (!producer.stop())
            { return false; }
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

    public final void registerProducer(Producer producer) {
        this.producer = producer;
    }

    public final void setDownstreaminstanceIds(List<String> ids) {
        this.downstreaminstanceIds = List.copyOf(ids);
    }
    
    protected void startHeartbeat() {
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
}
