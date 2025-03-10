package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.Event;
import utils.Pair;

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EventDeserializationStrategy implements DeserializationStrategy {

    // TODO: problem? Since JXES can represent several logs, several traces, and/or several events, a valid JXES can result in multiple events. But with the current interface, only a single event can be output
    @Override
    public DataType deserialize(String payload) {
        Map<String, Object> trace_attributes = new HashMap<>();
        Map<String, Object> event_attributes = new HashMap<>();

        // extract global attributes
        String globalAttrsStr = "\"global-attrs\":";
        int globalAttrsIndex = payload.indexOf(globalAttrsStr);
        Pair<String, Integer> globalAttrsContentsAndEnd = getNextCurlyBracketContent(payload, globalAttrsIndex+globalAttrsStr.length());
        String contents = globalAttrsContentsAndEnd.getFirst();
        String traceStr = "\"trace\"";
        int traceIndex = contents.indexOf(traceStr);
        if (traceIndex != -1) {
            Pair<String, Integer> traceContentsAndEnd = getNextCurlyBracketContent(contents, traceIndex+traceStr.length());
            String traceContents = traceContentsAndEnd.getFirst();

        }

        return new Event("not implemented", "not implemented", "not implemented", new HashSet<>());
    }

    private Pair<String, Integer> getNextCurlyBracketContent(String str, int startIndex) {
        int startCurlyIndex = str.indexOf('{',startIndex);
        int endCurlyIndex = str.indexOf('}', startCurlyIndex+1);
        assert startCurlyIndex != -1 && endCurlyIndex != -1 && (startCurlyIndex+1) < endCurlyIndex
                : "must have \" pair and non-empty content";

        String contents = str.substring(startCurlyIndex+1, endCurlyIndex);
        return new Pair<>(contents, endCurlyIndex+1);
    }

}
