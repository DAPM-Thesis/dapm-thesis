package communication.message.impl;

import communication.message.serialization.MessageVisitor;
import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.deserialization.TraceDeserializationStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Trace extends Message implements Iterable<Event> {

    private final List<Event> trace;
    private String caseID;

    public Trace(List<Event> trace) {
        super(new TraceDeserializationStrategy());
        assert trace != null;
        this.trace = new ArrayList<>();
        for (Event event : trace) { add(event); }
    }

    public List<Event> getTrace() { return trace; }
    public String getCaseID() { return caseID; }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return new TraceDeserializationStrategy();
    }

    public boolean add(Event event) {
        if (trace.isEmpty()) { this.caseID = event.getCaseID(); }
        assert event.getCaseID().equals(caseID) : String.format("All events in a trace must have the same case ID. (TraceCID, givenCID) = (\"%s\", \"%s\")", caseID, event.getCaseID());
        return trace.add(event);
    }

    public int length() { return trace.size(); }

    public boolean isEmpty() { return trace.isEmpty(); }

    @Override
    public Iterator<Event> iterator() {
        return trace.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trace otherTrace)) return false;
        return trace.equals(otherTrace.trace);
    }
}
