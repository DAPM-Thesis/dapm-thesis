package communication;

public interface Publisher<T> {
    void publish(T message);
    void registerProducer(Producer producer);
}

