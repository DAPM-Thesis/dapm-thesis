package communication;

import communication.config.ProducerConfig;

public interface Publisher<T> {
    void publish(T message);
    void registerProducer(ProducerConfig config);
}

