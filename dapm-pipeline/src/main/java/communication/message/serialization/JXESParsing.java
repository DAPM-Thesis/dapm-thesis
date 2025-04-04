package communication.message.serialization;

import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import utils.Pair;

import java.util.*;

public class JXESParsing extends JSONParsing {

    /** Parses attributes that are not the case ID, activity, and time stamp */
    public static Set<Attribute<?>> parseNonEssentialEventAttributes(Map<String, Object> traceGlobalAttributes,
                                                                     Map<String, Object> eventGlobalAttributes,
                                                                     Map<String, Object> traceAttributes,
                                                                     Map<String, Object> eventMap) {
        Map<String, Object> allAttributes = new HashMap<>(traceGlobalAttributes);
        allAttributes.putAll(eventGlobalAttributes);
        allAttributes.putAll(traceAttributes);
        allAttributes.putAll(eventMap);

        Set<Attribute<?>> attributes = new HashSet<>();
        for (Map.Entry<String, Object> keyValuePair : allAttributes.entrySet()) {
            String name = keyValuePair.getKey();
            if (isEssentialAttributeKey(name)) { continue; }
            Attribute<?> attr = parseAttribute(name, keyValuePair.getValue());
            attributes.add(attr);
        }
        return attributes;
    }

    public static Event getEvent(Map<String, Object> traceGlobalAttrs, Map<String, Object> eventGlobalAttrs, Map<String, Object> traceAttributes, Map<String, Object> eventMap) {
        assert eventMap.containsKey("\"date\"") : "No \"date\" (timestamp) in the given event. Timestamp must be in event - and not trace or global attributes; events are atomic and therefore must have distinct timestamps";
        assert eventMap.containsKey("\"concept:name\"") : "No \"concept:name\" (activity) in the given event. Activity must be in event - and not in trace or global attributes; this would be ambiguous since caseID is also called \"concept:name\"";

        String caseID = maybeRemoveOuterQuotes(JXESParsing.getCaseID(traceGlobalAttrs, traceAttributes));
        String activity = maybeRemoveOuterQuotes((String) eventMap.get("\"concept:name\""));
        String timestamp = maybeRemoveOuterQuotes((String) eventMap.get("\"date\""));
        Set<Attribute<?>> attributes = parseNonEssentialEventAttributes(traceGlobalAttrs, eventGlobalAttrs, traceAttributes, eventMap); // TODO: can be optimized; combine global and trace attributes in the loop, rather than in this method.
        return new Event(caseID, activity, timestamp, attributes);
    }

    private static boolean isEssentialAttributeKey(String key) {
        return key.equals("\"date\"") || key.equals("\"concept:name\"");
    }

    public static Pair<Map<String, Object>, Map<String, Object>> getTraceAndEventGlobalAttributes(Map<String, Object> jsonMap) {
        Map<String, HashMap<String, Object>> globalAttributes
                = (HashMap<String, HashMap<String, Object>>) jsonMap.get("\"global-attrs\"");
        if (globalAttributes == null) { return new Pair<>(new HashMap<>(), new HashMap<>()); }

        Map<String, Object> traceAttributes = globalAttributes.get("\"trace\"");
        if (traceAttributes == null) { traceAttributes = new HashMap<>(); }

        Map<String, Object> eventAttributes = globalAttributes.get("\"event\"");
        if (eventAttributes == null) { eventAttributes = new HashMap<>(); }

        return new Pair<>(traceAttributes, eventAttributes);
    }

}
