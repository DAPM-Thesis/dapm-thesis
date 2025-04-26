package pipeline.processingelement;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import utils.LogUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Override
    public boolean start() {
        try {
            executor.submit(() -> {
                try {
                    while (isAvailable()) {
                        O output = process();
                        publish(output);
                    }
                } catch (Exception e) {
                    LogUtil.error(e, "Exception in source.");
                }
            });
            LogUtil.info("Source started successfully.");
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to start source.");
            return false;
        }
    }

    public abstract O process();

    @Override
    public void publish(O data) { producer.publish(data); }

    @Override
    public boolean stop() {
        try {
            executor.shutdown();
            return true;
        }catch (Exception e) {
            LogUtil.error(e, "Failed to stop source.");
            return false;
        }
    }

    @Override
    public boolean terminate() {
        return producer.terminate();
    }

    @Override
    public void registerProducer(ProducerConfig config) {
        if(this.producer == null) {
            this.producer = new Producer(config);
        }
    }
}
