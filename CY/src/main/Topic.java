package main;

import main.datatype.DataType;
import main.node.handle.InputHandle;
import main.observerpattern.Publisher;
import main.observerpattern.Subscriber;

import java.util.Collection;
import java.util.HashSet;

public class Topic<T extends DataType> implements Publisher<T> {
    private Collection<Subscriber<T>> subscribers;

    public Topic() {
        subscribers = new HashSet<>();
    }

    @Override
    public void subscribe(Subscriber<T> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber<T> subscriber) {
        assert subscribers.contains(subscriber);
        subscribers.remove(subscriber);
    }

    @Override
    public void publish(T message) {
        for (Subscriber<T> subscriber : subscribers) {
            subscriber.observe(message);
        }
    }
}
