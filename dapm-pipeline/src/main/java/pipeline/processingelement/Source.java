package pipeline.processingelement;

import communication.Publisher;
import communication.Subscriber;

public abstract class Source<O> extends ProcessingElement implements Publisher<O> {
    private Publisher<O> producer; // Channel

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
    public void registerProducer(Publisher<O> producer) {
        this.producer = producer;
    }

    @Override
    public boolean unsubscribe(Subscriber<O> subscriber) {
        return true;
    }

}
