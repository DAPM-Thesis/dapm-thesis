package main.observerpattern;

import main.Message;

public interface Listener {

    void updateSubscriber(Message message);
}
