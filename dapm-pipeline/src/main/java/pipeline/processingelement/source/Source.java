package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public Source(Configuration configuration) { super(configuration); }

    @Override
    public void publish(O data) {
        producer.publish(data);
    }

    @Override
    public abstract boolean start();

    @Override
    public boolean pause() {
        return producer.pause();
    }

    @Override
    public boolean terminate() {
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

    @Override
    public void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
