package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;

import java.util.Map;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public Source(Configuration configuration) { super(configuration); }

    public abstract void start();

    @Override
    public void publish(O data) {
        producer.publish(data);
    }

    @Override
    public abstract boolean start();

    @Override
    public abstract boolean pause();

    @Override
    public boolean terminate() {
        boolean terminated = false;
        if (producer != null) {
           terminated = producer.terminate();
           if(terminated) {
               producer = null;
           }
        }
        return terminated;
    }

    @Override
    public void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
