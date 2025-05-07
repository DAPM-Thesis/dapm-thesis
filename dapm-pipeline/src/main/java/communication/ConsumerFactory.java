package communication;

import communication.config.ConsumerConfig;
import communication.message.Message;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public abstract class ConsumerFactory {

    @Lookup
    protected abstract Consumer createMessageListener();

    public void registerConsumer(Subscriber<Message> subscriber, ConsumerConfig config) {
        Consumer consumer = createMessageListener();
        consumer.registerListener(subscriber, config);
        subscriber.registerConsumer(consumer, config.portNumber());
    }
}