package communication.channel;

import communication.Publisher;
import communication.Subscriber;

import java.util.HashSet;
import java.util.Set;

public class Channel<T> {
    Set<Subscriber<T>> outgoing = new HashSet<>();
}
