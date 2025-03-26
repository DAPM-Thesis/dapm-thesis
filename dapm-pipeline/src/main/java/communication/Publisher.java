package communication;

public interface Publisher<T> {
    void publish(T message);
    boolean subscribe(Subscriber<T> subscriber);
    boolean unsubscribe(Subscriber<T> subscriber);
}
