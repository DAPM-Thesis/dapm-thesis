package main.observerpattern;

import main.Message;
import main.Topic;

import java.util.Collection;
import java.util.HashSet;

public interface Publisher<T> {
    public void subscribe(Subscriber<T> subscriber);

    public void unsubscribe(Subscriber<T> subscriber);

    public void publish(T message);
}
