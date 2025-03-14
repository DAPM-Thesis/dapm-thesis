package datatype.event;

import datatype.DataType;
import datatype.petrinet.PetriNet;
import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;
import datatype.serialization.deserialization.EventDeserializationStrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Event extends DataType {
    private final String caseID;
    private final String activity;
    private final String timestamp;
    private final Set<Attribute<?>> attributes;

    public Event(String caseID, String activity, String timestamp, Set<Attribute<?>> attributes) {
        this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.attributes = attributes;
    }

    public String getCaseID() {return caseID;}
    public String getActivity() {return activity; }
    public String getTimestamp() {return timestamp; }
    public Collection<Attribute<?>> getAttributes() {return attributes;}

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

    @Override
    public String toString() {
        return String.format("Event [caseID=%s, activity=%s, timestamp=%s, attributes=%s]",caseID,activity,timestamp,attributes);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Event otherEvent)) return false;
        return caseID.equals(otherEvent.getCaseID())
                && activity.equals(otherEvent.getActivity())
                && timestamp.equals(otherEvent.getTimestamp())
                && attributes.equals(otherEvent.attributes);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(caseID, activity, timestamp, attributes);
    }
}
