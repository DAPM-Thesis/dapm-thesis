package datatype.serialization;

import datatype.DataMap;
import datatype.event.Event;
import datatype.petrinet.PetriNet;

public interface DataTypeVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(DataMap dm);
}
