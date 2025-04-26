package communication;

import communication.config.ConsumerConfig;

public interface Subscriber<T> {
    void observe(T t, int portNumber);
    void registerConsumer(ConsumerConfig config);
}
