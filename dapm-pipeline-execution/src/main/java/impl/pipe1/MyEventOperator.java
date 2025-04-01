package impl.pipe1;

import algorithm.Algorithm;
import communication.message.impl.event.Event;
import pipeline.processingelement.operator.SimpleOperator;
import communication.message.Message;

import java.util.HashMap;
import java.util.Map;

public class MyEventOperator extends SimpleOperator<Event> {

    public MyEventOperator(Algorithm<Message, Event> algorithm) {
        super(algorithm);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

}
