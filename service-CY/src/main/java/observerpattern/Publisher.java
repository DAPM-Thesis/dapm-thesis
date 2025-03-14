package observerpattern;

public interface Publisher<T> {
    void subscribe(Subscriber<T> subscriber);

    void unsubscribe(Subscriber<T> subscriber);

    void publish(T message);
}
