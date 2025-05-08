package communication.message;

import communication.message.impl.Alignment;
import communication.message.impl.time.UTCTime;
import communication.message.impl.time.Date;
import communication.message.impl.Trace;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeRegistry {
    private static final Map<String, Class<? extends Message>> nameToClass = new HashMap<>();

    static {
        register("Event", Event.class);
        register("PetriNet", PetriNet.class);
        register("Alignment", Alignment.class);
        register("Trace", Trace.class);
        register("Time", Date.class);
        register("UTCTime", UTCTime.class);
    }

    public static Class<? extends Message> getMessageType(String simpleClassName) {
        if (!nameToClass.containsKey(simpleClassName)) { throw new IllegalArgumentException("Unknown message type: " + simpleClassName);}
        return nameToClass.get(simpleClassName);
    }

    private static void register(String simpleClassName, Class<? extends Message> messageClass) {
        nameToClass.put(simpleClassName, messageClass);
    }
}
