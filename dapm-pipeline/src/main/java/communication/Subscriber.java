package communication;

public interface Subscriber<T> {
    void observe(T t, int portNumber);
    void registerConsumer(Consumer listener, int portNumber);
}
