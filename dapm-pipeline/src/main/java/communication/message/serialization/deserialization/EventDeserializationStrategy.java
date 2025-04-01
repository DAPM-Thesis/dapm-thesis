package communication.message.serialization.deserialization;

import communication.message.serialization.JXESParsing;
import communication.message.Message;
import utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes a JXES-formatted string into an event. Assumes the string contains only a single event.
     *  Currently, this method does not support classifier values; the case ID will be the "concept:name" key's value in
     *  "traces" -> "attrs" or in the global-attrs (prioritizing the first over the latter), and the activity will be
     *  the value of the "concept:name" key in an event. Note also that a trace's "attrs" keys will also be added to the
     *  returned event's attributes. */
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jsonMap = JXESParsing.toJSONMap(payload);

        // since we assume only a single event can be deserialized at a time, all global attributes can be combined
        // and so can the trace "attrs" key:value pairs.
        Pair<Map<String, Object>, Map<String, Object>> traceAndEventGlobalAttributes = JXESParsing.getTraceAndEventGlobalAttributes(jsonMap);
        Map<String, Object> traceGlobalAttrs = traceAndEventGlobalAttributes.getFirst();
        Map<String, Object> eventGlobalAttrs = traceAndEventGlobalAttributes.getSecond();

        assert jsonMap.containsKey("\"traces\"") : "incorrect payload format.";
        List<Map<String, Object>> traces = (List<Map<String, Object>>) jsonMap.get("\"traces\"");
        assert traces != null && traces.size() == 1 : "event deserialization assumes exactly 1 trace";

        // get current trace and its attributes
        Map<String, Object> traceMap = traces.getFirst();
        Map<String, Object> traceAttributes = (traceMap.containsKey("\"attrs\"")) ? (Map<String, Object>) traceMap.get("\"attrs\"")
                : new HashMap<>();
        assert traceMap.containsKey("\"events\"") && ((List<Map<String, Object>>)traceMap.get("\"events\"")).size() == 1 : "trace must contain exactly 1 event.";
        List<Map<String, Object>> events = ((List<Map<String, Object>>) traceMap.get("\"events\""));
        assert events != null && events.size() == 1: "Assumes there is only 1 event!";
        Map<String, Object> eventMap = events.getFirst();

        return JXESParsing.getEvent(traceGlobalAttrs, eventGlobalAttrs, traceAttributes, eventMap);
    }

}
