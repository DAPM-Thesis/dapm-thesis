package communication.message;

import communication.message.impl.Alignment;
import communication.message.impl.Trace;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeRegistry {
    private static final Map<String, Class<? extends Message>> nameToClass = new HashMap<>();

    public static Class<? extends Message> getMessageType(String simpleClassName) {
        return nameToClass.get(simpleClassName);
    }

    static {
        register("Event", Event.class);
        register("PetriNet", PetriNet.class);
        register("Alignment", Alignment.class);
        register("Trace", Trace.class);
    }

    private static void register(String simpleClassName, Class<? extends Message> messageClass) {
        nameToClass.put(simpleClassName, messageClass);
    }
}
