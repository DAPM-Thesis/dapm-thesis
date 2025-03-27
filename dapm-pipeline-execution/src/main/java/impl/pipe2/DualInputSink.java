package impl.pipe2;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import pipeline.processingelement.Sink;

import java.util.HashMap;
import java.util.Map;

public class DualInputSink extends Sink {
    @Override
    public void observe(Message message) {
        System.out.println(this + " received: " + message);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        map.put(PetriNet.class, 1);
        return map;
    }
}
