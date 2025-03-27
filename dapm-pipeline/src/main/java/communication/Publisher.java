package communication;

public interface Publisher<T> {
    void publish(T message);
    void registerProducer(Publisher<T> producer);
    boolean unsubscribe(Subscriber<T> subscriber);
}

