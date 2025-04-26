package pipeline.processingelement;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import exceptions.PipelineExecutionException;
import utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Override
    public void start() {
        try {
            executor.submit(() -> {
                try {
                    while (isAvailable()) {
                        O output = process();
                        publish(output);
                    }
                } catch (Exception e) {
                    throw new PipelineExecutionException("Exception in source.", e);
                }
            });
        } catch (Exception e) {
            throw new PipelineExecutionException("Failed to start source.", e);
        }
    }

    public abstract O process();

    @Override
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
        } else {
            LogUtil.debug("Producer already registered for source.");
        }
    }
}
