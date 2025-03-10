package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;
import datatype.serialization.deserialization.EventDeserializationStrategy;

import java.util.Collection;
import java.util.HashSet;

public class Event extends DataType {
    private final String caseID;
    private final String activity;
    private final String timestamp;
    private final Collection<Attribute<?>> attributes;

    public Event(String caseID, String activity, String timestamp, HashSet<Attribute<?>> attributes) {
        this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.attributes = attributes;
    }

    public String getCaseID() {return caseID;}

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return new EventDeserializationStrategy();
    }
}
