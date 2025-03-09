package main.datatype.visitorpattern;

import main.datatype.Event;
import main.datatype.petrinet.PetriNet;
import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;
import main.datatype.petrinet.arc.Arc;

public interface Visitor<T> {
    T visitEvent(Event e);
    T visitPetriNet(PetriNet pn);
}
