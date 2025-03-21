package communication;

import java.util.HashSet;
import java.util.Set;

public class Channel<T> implements Subscriber<T>, Publisher<T> {
    Set<Subscriber<T>> outgoing = new HashSet<>();

    @Override
    public void publish(T message) {
        for (Subscriber<T> subscriber : outgoing) { subscriber.observe(message); }
    }

    @Override
    public boolean subscribe(Subscriber<T> subscriber) {
        if (outgoing.contains(subscriber)) { return true; }
        return outgoing.add(subscriber);
    }

    @Override
    public boolean unsubscribe(Subscriber<T> subscriber) {
        return outgoing.remove(subscriber);
    }

    @Override
    public void observe(T t) {

    }
}
