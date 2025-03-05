package main;

import main.datatype.DataType;
import main.observerpattern.Publisher;
import main.observerpattern.Subscriber;

import java.util.Collection;
import java.util.HashSet;

/** The component which the nodes will communicate over. In particular, output handles will publish to them, and
 * input handles will subscribe to them. */
public class Topic<T extends DataType> implements Publisher<Message<T>> {
    private Collection<Subscriber<Message<T>>> subscribers;

    public Topic() {
        subscribers = new HashSet<>();
    }

    @Override
    public void subscribe(Subscriber<Message<T>> subscriber) { subscribers.add(subscriber); }

    @Override
    public void unsubscribe(Subscriber<Message<T>> subscriber) {
        assert subscribers.contains(subscriber);
        subscribers.remove(subscriber);
    }

    @Override
    public void publish(Message<T> message) {
        for (Subscriber<Message<T>> subscriber : subscribers) {
            subscriber.observe(message);
        }
    }
}
