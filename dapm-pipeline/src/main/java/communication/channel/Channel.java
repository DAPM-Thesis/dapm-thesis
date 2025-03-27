package communication.channel;

import communication.Publisher;
import communication.Subscriber;
import communication.message.Message;

import java.util.HashSet;
import java.util.Set;

public class Channel implements Subscriber<Message>, Publisher<Message> {
    Set<Subscriber<Message>> outgoing = new HashSet<>();

    @Override
    public void publish(Message message) {
        for (Subscriber<Message> subscriber : outgoing) { subscriber.observe(message); }
    }

    @Override
    public boolean subscribe(Subscriber<Message> subscriber) {
        if (outgoing.contains(subscriber)) { return true; }
        return outgoing.add(subscriber);
    }

    @Override
    public boolean unsubscribe(Subscriber<Message> subscriber) {
        return outgoing.remove(subscriber);
    }

    @Override
    public void observe(Message t) {
        publish(t);
    }
}
