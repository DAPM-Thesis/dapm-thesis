package main.observerpattern;

public interface Subscriber<T> {

    void observe(T message);
}
