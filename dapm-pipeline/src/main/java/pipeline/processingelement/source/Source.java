package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
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
        if(producer != null) { // TODO: why would producer be null?
            producer.publish(data);
        }
    }

    @Override
    public void registerProducer(ProducerConfig config) {
        if(this.producer == null) {
            this.producer = new Producer(config);
        }
    }
}
