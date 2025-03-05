package main;

import main.datatype.DataType;
import main.observerpattern.Publisher;
import main.observerpattern.Subscriber;

import java.util.Collection;
import java.util.HashSet;

/** The component which the nodes will communicate over. In particular, output handles will publish to them, and
 * input handles will subscribe to them. */
public class Topic<T extends DataType> implements Publisher<Message> {
    private Collection<Subscriber<Message>> subscribers;

    public Topic() {
        subscribers = new HashSet<>();
    }

    @Override
    public void subscribe(Subscriber<Message> subscriber) { subscribers.add(subscriber); }

    @Override
    public void unsubscribe(Subscriber<Message> subscriber) {
        assert subscribers.contains(subscriber);
        subscribers.remove(subscriber);
    }

    @Override
    public void publish(Message message) {
        for (Subscriber<Message> subscriber : subscribers) {
            subscriber.observe(message);
        }
    }
}
