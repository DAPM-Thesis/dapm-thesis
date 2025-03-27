package communication.channel;

import communication.Producer;
import communication.Publisher;
import communication.Subscriber;
import communication.message.Message;

import java.util.HashSet;
import java.util.Set;

public class Channel implements Subscriber<Message>, Publisher<Message> {
    Set<Subscriber<Message>> outgoing = new HashSet<>();

    @Override
    public void publish(Message message) {

    }

    @Override
    public void registerProducer(Producer producer) {

    }

    @Override
    public boolean unsubscribe(Subscriber<Message> subscriber) {
        return false;
    }

    @Override
    public void observe(Message message) {

    }

    @Override
    public void registerConsumer(String topic) {

    }
}
