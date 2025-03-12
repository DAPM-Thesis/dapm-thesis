package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.event.Event;
import datatype.event.NestedAttribute;
import utils.Pair;
import datatype.event.Attribute;

import java.util.*;

public class EventDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes a JXES-formatted string into an event. Assumes the string contains only a single event.
     *  Currently, this method does not support classifier values; the case ID will be the "concept:name" key's value in
     *  "traces" -> "attrs" or in the global-attrs (prioritizing the first over the latter), and the activity will be
     *  the value of the "concept:name" key in an event. Note also that a trace's "attrs" keys will also be added to the
     *  returned event's attributes. */
    @Override
    public DataType deserialize(String payload) {
        Map<String, Object> jsonMap = getJsonMap(payload);

        // since we assume only a single event can be deserialized at a time, all global attributes can be combined
        // and so can the trace "attrs" key:value pairs.
        Map<String, Object> globalAttributes = getGlobalAttributes(jsonMap);
        globalAttributes.putAll(getTracesAttributes(jsonMap));

        // parse event
        List<Map<String, Object>> events = (List<Map<String, Object>>) jsonMap.get("\"events\"");
        assert events != null && events.size() == 1: "incorrectly formatted events";
        Map<String, Object> eventMap = events.get(0);
        String identifier = "\"concept:name\"";
        String caseID = (String) globalAttributes.get(identifier); // the caseID must be a string
        String activity = (String) eventMap.get(identifier);
        String timestamp = getTimestamp(globalAttributes, eventMap);
        List<Attribute> attributes = parseNonEssentialAttributes(globalAttributes, eventMap);

        return null;
    }

    private List<Attribute> parseNonEssentialAttributes(Map<String, Object> globalAttributes, Map<String, Object> eventMap) {
        globalAttributes.putAll(eventMap); // merge the two maps, keeping eventMap values when both have the same key
        for (Map.Entry<String, Object> entry : globalAttributes.entrySet()) {
            String name = entry.getKey();
            if (isEssentialAttributeKey(name)) { continue; }

            Attribute attr = parseAttribute(entry.getValue());

        }
    }

    private Attribute parseAttribute(Object value) {
        String valueStr = (String) value;
        if (isContainer(valueStr)) {
            Attribute nestedAttr = new NestedAttribute();
// TODO: implement
// TODO: figure out if actually the JSON-map is always a tree where nodes are hashmaps and values therefore always are
//  strings/hashmaps; figure out through debugging.
        }
    }

    private boolean isEssentialAttributeKey(String key) {
        return key.equals("\"date\"") || key.equals("\"concept:name\"");
    }

    /** Returns the eventMap timestamp if it exists, otherwise the globalAttributes timestamp, otherwise "". The key
     *  that holds the timestamp is \"date\" */
    private String getTimestamp(Map<String, Object> globalAttributes, Map<String, Object> eventMap) {
        String dateStr = "\"date\"";

        // the date value is always a String, so it can safely be cast.
        if (eventMap.containsKey(dateStr)) {
            return (String) eventMap.get(dateStr);
        }
        else if (globalAttributes.containsKey(dateStr)) {
            return (String) globalAttributes.get(dateStr);
        } else {
            return "";
        }
    }

    /**  */
    private Map<String, Object> getTracesAttributes(Map<String, Object> jsonMap) {
        // we can safely cast since JXES always has (in python syntax) jsonMap["traces"][0]["attrs"] = stringKey:value map
        List<Map<String, Map<String, Object>>> traces = (List<Map<String, Map<String, Object>>>) jsonMap.get("\"traces\"");
        assert traces != null : String.format("invalid format of jsonMap: %s", jsonMap);

        Map<String, Object> traceAttributes = traces.get(0).get("\"attrs\"");
        assert traceAttributes != null : String.format("invalid format of jsonMap: %s", jsonMap);

        return traceAttributes;
    }

    private Map<String, Object> getGlobalAttributes(Map<String, Object> jsonMap) {
        Map<String, HashMap<String, Object>> globalAttributes =
                (HashMap<String, HashMap<String, Object>>) jsonMap.get("\"global-attrs\"");

        if (globalAttributes == null) { return new HashMap<>(); }

        HashMap<String, Object> allAttrs = globalAttributes.get("trace");
        HashMap<String, Object> eventAttrs = globalAttributes.get("event");
        if (allAttrs == null) { allAttrs = new HashMap<>(); }
        if (eventAttrs == null) { eventAttrs = new HashMap<>(); }

        allAttrs.putAll(eventAttrs);

        return allAttrs;
    }


    /** Recursively parses a json-formatted String to extract a json-like Map.
     *  @param jsonStr a json-formatted String.
     *  @return A nested map representing the given json string; It has a tree-like structure where a non-leaf node is
     *  a Hashmap from String to Hashmap, and a leaf is a String. When decoded, a value that is supposed to be a string
     * will be wrapped in quotation marks, whereas a non-string will not. Hence the leaf "\"hi\"" is a String, but
     * "5.0" is not [it is a float]. */
    private Map<String, Object> getJsonMap(String jsonStr) {
        return getContainer(jsonStr);
    }

    private Map<String, Object> getContainer(String container) {
        Object value = new Object();
        Map<String, Object> containerMap = new HashMap<>();
        String contents = unWrap(container, '{', '}');
        List<String> keyValuePairs = commaSplit(contents);
        for (String pair : keyValuePairs) { // pair has format key:value [potentially with whitespace chars between]
            Pair<String, String> keyAndValue = splitAndStripKeyAndValue(pair);
            String name = keyAndValue.getFirst();
            String valueStr = keyAndValue.getSecond();
            if (isContainer(valueStr)) {
                value = getContainer(valueStr); // dynamic type: Map<String, Object>
            } else if (isList(valueStr)) {
                value = getList(valueStr); // dynamic type: List<Map<String, Object>>  <-- list of containers
            } else {
                value = getStringValue(valueStr); // dynamic type: String
            }
            containerMap.put(name, value);
        }
        return containerMap;
    }

    private Object getStringValue(String valueStr) {
        return valueStr.strip();
    }


    /** returns the list of containers in the given list ('[' and ']' wrapped) string */
    private List<Map<String, Object>> getList(String listStr) {
        List<Map<String, Object>> containers = new ArrayList<>();
        listStr = unWrap(listStr, '[', ']');
        List<String> elements = commaSplit(listStr);
        for (String container : elements) {
           containers.add(getContainer(container));
        }
        return containers;
    }

    private Pair<String, String> splitAndStripKeyAndValue(String pair) {
        /* the pair is always of the form key:pair, where the key is a quotation-wrapped character sequence which may
         * contain its own ':' (colon). */
        // find the closing quotation mark
        int endQuoteIndex = pair.indexOf('\"');
        assert endQuoteIndex != -1 : String.format("no starting quotation in: %s", pair);
        endQuoteIndex = pair.indexOf('\"', endQuoteIndex+1);
        assert endQuoteIndex != -1 : String.format("no closing quotation in: %s", pair);

        String key = pair.substring(0, endQuoteIndex).strip();
        String value = pair.substring(endQuoteIndex+1).strip();
        return new Pair<>(key, value);
    }


    /** @param contents An unwrapped list/container.
     *  @return The strings between commas of the contents input. The strings will be stripped of whitespace, \n, \t,
     *  and \r in both ends. */
    private List<String> commaSplit(String contents) {
        // since contents can be nested [they can contain lists/containers], we must only split at the current level
        int openedCurly = 0;
        int openedSquare = 0;
        int currentStart = 0;

        List<String> commaSeparatedStrings = new ArrayList<>();

        for (int i = 0; i < contents.length(); i++) {
            char ch = contents.charAt(i);
            if (ch == ',' && openedCurly == 0 && openedSquare == 0) {
                commaSeparatedStrings.add(contents.substring(currentStart, i-1));
                currentStart = i+1;
            }
            else if (ch == '{') { openedCurly++; }
            else if (ch == '}') { openedCurly--; }
            else if (ch == '[') { openedSquare++; }
            else if (ch == ']') { openedSquare--; }
        }
        // remember to add the last
        commaSeparatedStrings.add(contents.substring(currentStart));
        return commaSeparatedStrings;
    }

    private String unWrap(String str, char startWrapper, int endWrapper) {
        int startIndex = str.indexOf(startWrapper);
        int endIndex = str.lastIndexOf(endWrapper);
        return str.substring(startIndex+1, endIndex);
    }

    private boolean isContainer(String str) {
        return isWrapped(str, '{', '}');
    }

    private boolean isList(String str) {
        return isWrapped(str, '[', ']');
    }

    /** A string is wrapped iff its content is wrapped by 'start' and 'end'. */
    private boolean isWrapped(String str, char start, char end) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r')
                {continue;}
            else if (ch == start) { return true;}
            assert (ch != end) : "there was no '"+ start +"' before this '"+ end + "'";
        }
        return false;
    }

}
