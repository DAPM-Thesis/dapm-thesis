package communication.message.serialization;

import communication.message.impl.Alignment;
import communication.message.impl.Trace;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
}
