package pipeline.processingelement.source;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.ProcessingElement;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import exceptions.PipelineExecutionException;
import utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel

    public abstract void start();

    public void publish(O data) {
        if (producer == null) {
            throw new IllegalStateException("Producer not registered for source");
        }
        producer.publish(data);
    }


    @Override
    public void stop() {
        try {
            executor.shutdown();
        } catch (Exception e) {
            throw new PipelineExecutionException("Failed to stop source.", e);
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
