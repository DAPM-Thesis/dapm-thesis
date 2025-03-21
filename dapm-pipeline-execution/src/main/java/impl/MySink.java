package impl;

import pipeline.processingelement.Sink;
import datatype.impl.event.Event;

public class MySink extends Sink<Event> {

    @Override
    public void observe(Event event) {
        System.out.println("Sink received: " + event);
    }
}
