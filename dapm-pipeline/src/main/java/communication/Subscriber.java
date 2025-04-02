package communication;

public interface Subscriber<T> {
    void observe(T t);
    void registerConsumer(String connectionTopic, String brokerURL);
}
