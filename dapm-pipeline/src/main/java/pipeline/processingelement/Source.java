package pipeline.processingelement;

import communication.Producer;
import communication.Publisher;
import communication.message.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O> {
    private Producer producer; // Channel
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    public void start() {
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
    public void registerProducer(String connectionTopic, String brokerURL) {
        if(this.producer == null) {
            this.producer = new Producer(connectionTopic, brokerURL);
        }
    }
}
