package main;

import main.datatype.Event;

public class Message {
    private Event event;

    public Message(Event event){
        this.event = event;
    }

    public Event getEvent() {return event;}
}
