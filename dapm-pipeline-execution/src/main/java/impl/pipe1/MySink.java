package impl.pipe1;

import pipeline.processingelement.Sink;
import communication.message.Message;
import communication.message.impl.event.Event;

import java.util.HashMap;
import java.util.Map;

public class MySink extends Sink {

    @Override
    public void observe(Message message) {
        System.out.println(this + " received: " + message);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
