package message.serialization;

import message.impl.Alignment;
import message.impl.Trace;
import message.impl.event.Event;
import message.impl.petrinet.PetriNet;

public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
}
