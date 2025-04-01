package impl.pipe1;

import algorithm.Algorithm;
import communication.message.impl.event.Event;
import communication.message.Message;

public class MyEventAlgorithm implements Algorithm<Message, Event> {

    @Override
    public Event run(Message event) {
        System.out.println(this.getClass().getSimpleName() + " applied.");
        return (Event) event;
    }
}
