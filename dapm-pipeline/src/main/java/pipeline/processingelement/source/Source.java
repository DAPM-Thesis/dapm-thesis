package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
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
