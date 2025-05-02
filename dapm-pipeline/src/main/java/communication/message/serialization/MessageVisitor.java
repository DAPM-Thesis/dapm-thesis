package communication.message.serialization;

import communication.message.impl.Alignment;
import communication.message.impl.InstantTime;
import communication.message.impl.Time;
import communication.message.impl.Trace;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
    T visit(Time time);
    T visit(InstantTime instantTime);
}
