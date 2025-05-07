package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

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
