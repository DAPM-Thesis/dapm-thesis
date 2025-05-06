package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public abstract void start();

    public abstract void pause();

    public void publish(O data) {
        if(producer != null) { // TODO: why would producer be null?
            producer.publish(data);
        }
    }

    @Override
    public void terminate() {
        if (producer != null) {
            producer.terminate();
            producer = null;
        }
    }

    @Override
    public void registerProducer(ProducerConfig config) {
        if (this.producer == null) {
            this.producer = new Producer(config);
        }
    }
}
