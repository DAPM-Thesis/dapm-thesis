package datatype.serialization.deserialization;

import datatype.impl.Alignment;
import datatype.DataType;
import datatype.impl.Trace;
import datatype.impl.event.Event;
import datatype.serialization.JXESParsing;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlignmentDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes A JXES-formatted string into an alignment. Assumes the given string contains exactly 2 traces:
     *  The first trace is set to the log trace, and the second trace is set to the model trace. */
    @Override
    public DataType deserialize(String payload) {
        Map<String, Object> jsonMap = JXESParsing.toJSONMap(payload);

        Pair<Map<String, Object>, Map<String, Object>> traceAndEventGlobalAttributes = JXESParsing.getTraceAndEventGlobalAttributes(jsonMap);
        Map<String, Object> traceGlobalAttrs = traceAndEventGlobalAttributes.getFirst();
        Map<String, Object> eventGlobalAttrs = traceAndEventGlobalAttributes.getSecond();

        assert jsonMap.containsKey("\"traces\"") : "incorrect payload format.";
        List<Map<String, Object>> traces = (List<Map<String, Object>>) jsonMap.get("\"traces\"");
        assert traces != null && !traces.isEmpty() : "trace deserialization currently assumes exactly 1 trace";

        List<Trace> alignmentTraces = new ArrayList<>(2);
        for (Map<String, Object> traceMap : traces) {
            Map<String, Object> traceAttributes = (traceMap.containsKey("\"attrs\"")) ? (Map<String, Object>) traceMap.get("\"attrs\"")
                                                                                      : new HashMap<>();
            assert traceMap.containsKey("\"events\"") && !((List<Map<String, Object>>)traceMap.get("\"events\"")).isEmpty() : "trace must contain at least 1 event.";

            // extract all events in the trace
            List<Event> trace = new ArrayList<>();
            for (Map<String, Object> eventMap : (List<Map<String, Object>>) traceMap.get("\"events\"")) {
                Event event = JXESParsing.getEvent(traceGlobalAttrs, eventGlobalAttrs, traceAttributes, eventMap);
                trace.add(event);
            }
            alignmentTraces.add(new Trace(trace));
        }
        assert alignmentTraces.size() == 2 : String.format("An alignment should only contain two traces, but here contains: %d", alignmentTraces.size());

        return new Alignment(alignmentTraces.get(0), alignmentTraces.get(1));
    }

}
