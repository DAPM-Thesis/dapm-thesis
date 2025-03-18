package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.event.Event;
import datatype.serialization.Parsing;
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
        Map<String, Object> jsonMap = Parsing.toJSONMap(payload);

        // since we assume only a single event can be deserialized at a time, all global attributes can be combined
        // and so can the trace "attrs" key:value pairs.
        Map<String, Object> globalAttributes = getGlobalAttributes(jsonMap);
        globalAttributes.putAll(getTracesAttributes(jsonMap));

        // parse event.
        List<Map<String, Object>> events = getEvents(jsonMap);
        assert events != null && events.size() == 1: "currently assumes there is only 1 event!";
        Map<String, Object> eventMap = events.getFirst();
        String identifier = "\"concept:name\"";
        String caseID = (String) globalAttributes.get(identifier); // the caseID must be a string
        String activity = (String) eventMap.get(identifier);
        String timestamp = getTimestamp(globalAttributes, eventMap);
        Set<Attribute<?>> attributes = parseNonEssentialAttributes(globalAttributes, eventMap);

        return new Event(maybeRemoveOuterQuotes(caseID),
                maybeRemoveOuterQuotes(activity),
                maybeRemoveOuterQuotes(timestamp), attributes) ;
    }

    private String maybeRemoveOuterQuotes(String str) {
        if (Parsing.isWrapped(str, '\"', '\"')) {
            return Parsing.unWrap(str, '\"', '\"');
        }
        return str;
    }

    /** currently assumes there is only 1 trace in "traces" */
    private List<Map<String, Object>> getEvents(Map<String, Object> jsonMap) {
        List<Map<String, List<Map<String, Object>>>> traces = (List<Map<String, List<Map<String, Object>>>>) jsonMap.get("\"traces\"");
        assert traces != null && traces.size() == 1: "incorrectly formatted traces";
        List<Map<String, Object>> events = traces.getFirst().get("\"events\"");
        assert events != null;
        return events;
    }

    /** Parses attributes that are not the case ID, activity, and time stamp */
    private Set<Attribute<?>> parseNonEssentialAttributes(Map<String, Object> globalAttributes, Map<String, Object> eventMap) {
        globalAttributes.putAll(eventMap); // merge the two maps, keeping eventMap values when both have the same key
        Set<Attribute<?>> attributes = new HashSet<>();
        for (Map.Entry<String, Object> entry : globalAttributes.entrySet()) {
            String name = entry.getKey();
            if (isEssentialAttributeKey(name)) { continue; }
            Attribute<?> attr = parseAttribute(name, entry.getValue());
            attributes.add(attr);
        }
        return attributes;
    }

    private Attribute<?> parseAttribute(String name, Object value) {
        name = maybeRemoveOuterQuotes(name);

        if (value instanceof Map<?, ?> && isNestedAttribute((Map<String, Object>) value)) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object nestedAttrValue = parseAttrValue(map.get("\"value\""));
            Map<String, Attribute<?>> nestedAttrs = getNestedAttributes((Map<String, Object>) map.get("\"nested-attrs\""));
            return new Attribute<>(name, nestedAttrValue, nestedAttrs);
        } else {
            return new Attribute<>(name, parseAttrValue(value));
        }
    }

    private Object parseAttrValue(Object value) {
        switch (value) {
            case String valueStr -> {
                return getSimpleAttributeValue(valueStr);
            }
            case Map<?, ?> map -> {
                Map<String, Object> container = (Map<String, Object>) value;
                return getNestedAttributes(container);
            }
            case List<?> list -> {
                List<Object> resultingList = new ArrayList<>();
                for (Object elem : list) {
                    resultingList.add(parseAttrValue(elem));
                }
                return resultingList;
            }
            case null, default -> throw new IllegalStateException("Unsupported type: " + value);
        }
    }

    private Object getSimpleAttributeValue(String value) {
        assert !value.isEmpty() : "string may not be empty [but is allowed to contain empty quotation marks, i.e. '\"\"']";
        if (isStringValue(value)) { return Parsing.unWrap(value, '"', '"'); }
        else if (isBooleanValue(value)) {return Boolean.parseBoolean(value); }
        else if (isInteger(value)) { return Integer.parseInt(value); }
        else if (isDouble(value)) { return Double.parseDouble(value); }
        else { throw new IllegalStateException("Unsupported type: " + value); }
    }

    private Map<String, Attribute<?>> getNestedAttributes(Map<String, Object> container) {
        Map<String, Attribute<?>> nestedAttributes = new HashMap<>();
        for (Map.Entry<String, Object> entry : container.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            nestedAttributes.put(name, parseAttribute(name, value));
        }
        return nestedAttributes;
    }

    private boolean isNestedAttribute(Map<String, Object> map) {
        // "value" and "nested-attrs" are keywords reserved for nested attributes.
        return map.containsKey("\"value\"") && map.containsKey("\"nested-attrs\"");
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBooleanValue(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private boolean isStringValue(String value) {
        return Parsing.isWrapped(value, '"', '"');
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

        Map<String, Object> traceAttributes = traces.getFirst().get("\"attrs\"");
        assert traceAttributes != null : String.format("invalid format of jsonMap: %s", jsonMap);

        return traceAttributes;
    }

    private Map<String, Object> getGlobalAttributes(Map<String, Object> jsonMap) {
        Map<String, HashMap<String, Object>> globalAttributes =
                (HashMap<String, HashMap<String, Object>>) jsonMap.get("\"global-attrs\"");

        if (globalAttributes == null) { return new HashMap<>(); }

        HashMap<String, Object> allAttrs = globalAttributes.get("\"trace\"");
        HashMap<String, Object> eventAttrs = globalAttributes.get("\"event\"");
        if (allAttrs == null) { allAttrs = new HashMap<>(); }
        if (eventAttrs == null) { eventAttrs = new HashMap<>(); }

        allAttrs.putAll(eventAttrs);

        return allAttrs;
    }


}
