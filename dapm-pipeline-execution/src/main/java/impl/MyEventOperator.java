package impl;

import algorithm.Algorithm;
import message.impl.event.Event;
import message.serialization.MessageSerializer;
import message.serialization.deserialization.MessageFactory;
import pipeline.processingelement.Operator;

public class MyEventOperator extends Operator<Event, Event, String, String> {

    private final MessageSerializer messageSerializer;

    public MyEventOperator(Algorithm<String, String> algorithm) {
        super(algorithm);
        messageSerializer = new MessageSerializer();
    }

    @Override
    protected boolean publishCondition(String algorithmOutput) {
        return true;
    }

    @Override
    protected String convertInput(Event event) {
        return messageSerializer.visit(event);
    }

    @Override
    protected Event convertOutput(String s) {
        return (Event) MessageFactory.deserialize(s);
    }
}
