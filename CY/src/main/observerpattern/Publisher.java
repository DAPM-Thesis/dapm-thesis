package main.observerpattern;

import main.Message;
import main.Topic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Publisher {
    HashMap<Topic, Collection<Listener>> listeners;

    public Publisher(HashMap<Topic, Collection<Listener>> listeners) {
        this.listeners = listeners;
    }

    public void subscribe(Topic topic, Listener listener) {
        if (!listeners.containsKey(topic)) {
            listeners.put(topic, new HashSet<Listener>());
        }
        listeners.get(topic).add(listener);
    }

    public void unsubscribe(Topic topic, Listener listener) {
        assert listeners.containsKey(topic) : String.format(" Tried to unsubscribe (%s) from a non-existent topic (%s)", listener, topic);
        assert listeners.get(topic).contains(listener) : String.format(" Tried to unsubscribe a listener from a topic (%s) it is not subscribed to", topic);

        listeners.get(topic).remove(listener);
    }

    public void notify(Topic topic, Message message) {
        for (Listener listener : listeners.get(topic)) {
            listener.updateSubscriber(message);
        }
    }
}
