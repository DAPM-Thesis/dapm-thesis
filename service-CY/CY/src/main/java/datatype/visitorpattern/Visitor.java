package datatype.visitorpattern;

import datatype.Event;
import datatype.petrinet.PetriNet;
import datatype.petrinet.Place;
import datatype.petrinet.Transition;
import datatype.petrinet.arc.Arc;

public interface Visitor<T> {
    T visitEvent(Event e);
    T visitPetriNet(PetriNet pn);
}
