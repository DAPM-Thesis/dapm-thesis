package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Event;

public class JXESParser {

    public static String eventToJXES(Event event) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode eventJson = objectMapper.createObjectNode();

            eventJson.put("case:id", event.getCaseId());
            eventJson.put("concept:name", event.getActivity());
            eventJson.put("time:timestamp", event.getTimeStamp().toString());

            ObjectNode traceEventJson = objectMapper.createObjectNode();
            traceEventJson.set("event", eventJson);

            ArrayNode traceArray = objectMapper.createArrayNode();
            traceArray.add(traceEventJson);

            ObjectNode traceJson = objectMapper.createObjectNode();
            traceJson.set("trace", traceArray);

            return objectMapper.writeValueAsString(traceJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting event to JXES", e);
        }
    }
}
