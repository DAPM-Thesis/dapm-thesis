package communication;

public interface Publisher<T> {
    void publish(T message);
    void registerProducer(String connectionTopic, String brokerURL);
}

