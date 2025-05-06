package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public void publish(O data) {
        if(producer != null) { // TODO: why would producer be null?
            producer.publish(data);
        }
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
    public void registerProducer(ProducerConfig config) {
        if (this.producer == null) {
            this.producer = new Producer(config);
        }
    }
}
