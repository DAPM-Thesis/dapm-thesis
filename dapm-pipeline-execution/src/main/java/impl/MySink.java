package impl;

import pipeline.processingelement.Sink;
import message.impl.event.Event;

public class MySink extends Sink<Event> {

    @Override
    public void observe(Event event) {
        System.out.println(this + " received: " + event);
    }
}
