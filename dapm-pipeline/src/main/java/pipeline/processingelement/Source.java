package pipeline.processingelement;

import communication.Producer;
import communication.Publisher;
import communication.Subscriber;
import communication.message.Message;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public void start() {
        while(isAvailable()) {
            O output = process();
            publish(output);
        }
    }

    public abstract O process();

    @Override
    public void publish(O data) { producer.publish(data); }

    @Override
    public void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
