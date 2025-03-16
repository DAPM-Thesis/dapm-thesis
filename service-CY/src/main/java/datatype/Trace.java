package datatype;

import datatype.event.Event;
import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;
import jakarta.annotation.Nonnull;

import java.util.*;

public class Trace extends DataType implements Iterable<Event> {

    private final List<Event> trace;
    private String caseID;

    public Trace(List<Event> trace) {
        assert trace != null;
        this.trace = new ArrayList<>();
        for (Event event : trace) { add(event); }
    }

    public List<Event> getTrace() { return trace; }
    public String getCaseID() { return caseID; }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return null;
    }

    public boolean add(Event event) {
        if (trace.isEmpty()) { this.caseID = event.getCaseID(); }
        assert event.getCaseID().equals(caseID);
        return trace.add(event);
    }

    public int length() { return trace.size(); }

    public boolean isEmpty() { return trace.isEmpty(); }

    @Override @Nonnull
    public Iterator<Event> iterator() {
        return trace.iterator();
    }
}
