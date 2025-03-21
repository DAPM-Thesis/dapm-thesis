package impl;

import algorithm.Algorithm;
import datatype.impl.event.Event;
import datatype.serialization.DataTypeSerializer;
import pipeline.processingelement.Operator;

public class MyOperator extends Operator<Event, String, String, String> {

    private final DataTypeSerializer dataTypeSerializer;

    public MyOperator(Algorithm<String, String> algorithm) {
        super(algorithm);
        dataTypeSerializer = new DataTypeSerializer();
    }

    @Override
    protected boolean publishCondition() {
        return true;
    }

    @Override
    protected String convertInput(Event event) {
        return dataTypeSerializer.visit(event);
    }

    @Override
    protected String convertOutput(String s) {
        return s;
    }
}
