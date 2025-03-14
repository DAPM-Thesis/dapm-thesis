package datatype;

import datatype.event.Event;
import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;

import java.util.List;

public class Trace extends DataType{
    private final List<Event> trace;

    public Trace(List<Event> trace) {
        this.trace = trace;
    }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return null;
    }

    public int length() {
        return trace.size();
    }

}
