package communication.message.serialization.deserialization;

import communication.message.Message;
import communication.message.impl.Trace;
import communication.message.serialization.JXESParsing;
import communication.message.impl.event.Event;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jsonMap = JXESParsing.toJSONMap(payload);

        Pair<Map<String, Object>, Map<String, Object>> traceAndEventGlobalAttributes = JXESParsing.getTraceAndEventGlobalAttributes(jsonMap);
        Map<String, Object> traceGlobalAttrs = traceAndEventGlobalAttributes.getFirst();
        Map<String, Object> eventGlobalAttrs = traceAndEventGlobalAttributes.getSecond();

        assert jsonMap.containsKey("\"traces\"") : "incorrect payload format.";
        List<Map<String, Object>> traces = (List<Map<String, Object>>) jsonMap.get("\"traces\"");
        assert traces != null && traces.size() == 1 : "trace deserialization assumes exactly 1 trace";

        // get current trace and its attributes
        Map<String, Object> traceMap = traces.getFirst();
        Map<String, Object> traceAttributes = (traceMap.containsKey("\"attrs\"")) ? (Map<String, Object>) traceMap.get("\"attrs\"")
                : new HashMap<>();
        assert traceMap.containsKey("\"events\"") && !((List<Map<String, Object>>)traceMap.get("\"events\"")).isEmpty() : "trace must contain at least 1 event.";

        // extract all events in the trace
        List<Event> trace = new ArrayList<>();
        for (Map<String, Object> eventMap : (List<Map<String, Object>>) traceMap.get("\"events\"")) {
            Event event = JXESParsing.getEvent(traceGlobalAttrs, eventGlobalAttrs, traceAttributes, eventMap);
            trace.add(event);
        }

        return new Trace(trace);
    }

}
