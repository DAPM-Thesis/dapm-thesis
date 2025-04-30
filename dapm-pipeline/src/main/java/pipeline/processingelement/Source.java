package pipeline.processingelement;

import communication.Producer;
import communication.Publisher;
import communication.config.ProducerConfig;
import communication.message.Message;
import pipeline.processingelement.accesscontrolled.AccessControlledProcessingElement;
import pipeline.processingelement.accesscontrolled.PEToken;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Source<O extends Message> extends AccessControlledProcessingElement implements Publisher<O> {
    protected Source(PEToken initialToken) {
        super(initialToken);
    }

    private Producer producer; // Channel
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public void start() {
        if (getHeartbeatManager() != null) getHeartbeatManager().start();
        executor.submit(() -> {
        while(isAvailable()) {
            O output = process();
            publish(output);
        }
        });
    }

    public abstract O process();

    @Override
    public void publish(O data) { producer.publish(data); }

    @Override
    public void registerProducer(ProducerConfig config) {
        if(this.producer == null) {
            this.producer = new Producer(config);
        }
    }
}
