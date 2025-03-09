package main.datatype.serialization;

import main.datatype.Event;
import main.datatype.petrinet.PetriNet;

public interface DataTypeVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
}
