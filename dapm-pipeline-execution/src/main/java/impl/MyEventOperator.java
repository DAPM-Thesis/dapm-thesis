package impl;

import algorithm.Algorithm;
import datatype.impl.event.Event;
import datatype.serialization.DataTypeSerializer;
import datatype.serialization.deserialization.DataTypeFactory;
import pipeline.processingelement.Operator;

public class MyEventOperator extends Operator<Event, Event, String, String> {

    private final DataTypeSerializer dataTypeSerializer;

    public MyEventOperator(Algorithm<String, String> algorithm) {
        super(algorithm);
        dataTypeSerializer = new DataTypeSerializer();
    }

    @Override
    protected boolean publishCondition(String algorithmOutput) {
        return true;
    }

    @Override
    protected String convertInput(Event event) {
        return dataTypeSerializer.visit(event);
    }

    @Override
    protected Event convertOutput(String s) {
        return (Event) DataTypeFactory.deserialize(s);
    }
}
