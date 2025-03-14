package datatype.serialization;

import datatype.Alignment;
import datatype.DataMap;
import datatype.Trace;
import datatype.event.Event;
import datatype.petrinet.PetriNet;

public interface DataTypeVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(DataMap dm);
    T visit(Trace t);
    T visit(Alignment a);
}
