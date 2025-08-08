package communication.message.serialization;

import communication.message.impl.*;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.time.UTCTime;
import communication.message.impl.time.Date;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
    T visit(Date time);
    T visit(UTCTime UTCTime);
    T visit(Metrics metrics);
    T visit(ProcessMap processMap);
    T visit(CausalNet causalNet);
    T visit(SoftConformanceReport softConformanceReport);
}
