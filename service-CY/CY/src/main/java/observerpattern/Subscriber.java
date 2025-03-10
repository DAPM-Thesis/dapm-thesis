package observerpattern;

public interface Subscriber<T> {

    void observe(T message);
}
