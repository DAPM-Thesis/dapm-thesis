package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.accesscontrolled.AccessControlledProcessingElement;
import security.token.PEToken;

public abstract class Source<O extends Message> extends AccessControlledProcessingElement implements Publisher<O> {
    protected Source(PEToken initialToken) {
        super(initialToken);
    }

    private Producer producer; // Channel

    public abstract void start();

    @Override
    public void publish(O data) {
        if(producer != null) {
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
