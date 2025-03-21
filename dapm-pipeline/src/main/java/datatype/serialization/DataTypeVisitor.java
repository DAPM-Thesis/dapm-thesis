package datatype.serialization;

import datatype.impl.Alignment;
import datatype.impl.Trace;
import datatype.impl.event.Event;
import datatype.impl.petrinet.PetriNet;

public interface DataTypeVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
}
