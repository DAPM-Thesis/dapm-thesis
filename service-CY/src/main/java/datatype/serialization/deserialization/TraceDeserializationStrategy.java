package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.Trace;
import datatype.event.Attribute;
import datatype.event.Event;
import datatype.serialization.Parsing;
import utils.Pair;

import java.util.*;

public class TraceDeserializationStrategy implements DeserializationStrategy {
    @Override
    public DataType deserialize(String payload) {
        Map<String, Object> jsonMap = Parsing.toJSONMap(payload);

        Pair<Map<String, Object>, Map<String, Object>> traceAndEventGlobalAttributes = getTraceAndEventGlobalAttributes(jsonMap);
        Map<String, Object> traceGlobalAttrs = traceAndEventGlobalAttributes.getFirst();
        Map<String, Object> eventGlobalAttrs = traceAndEventGlobalAttributes.getSecond();

        assert jsonMap.containsKey("\"traces\"") : "incorrect payload format.";
        List<Map<String, Object>> traces = (List<Map<String, Object>>) jsonMap.get("\"traces\"");
        assert traces != null && traces.size() == 1 : "trace deserialization currently assumes exactly 1 trace";

        // get current trace and its attributes
        Map<String, Object> traceMap = traces.getFirst();
        Map<String, Object> traceAttributes = (traceMap.containsKey("\"attrs\"")) ? (Map<String, Object>) traceMap.get("\"attrs\"")
                : new HashMap<>();
        assert traceMap.containsKey("\"events\"") && !((List<Map<String, Object>>)traceMap.get("\"events\"")).isEmpty() : "trace must contain at least 1 event.";

        // extract all events in the trace
        List<Event> trace = new ArrayList<>();
        for (Map<String, Object> eventMap : (List<Map<String, Object>>) traceMap.get("\"events\"")) {
            assert eventMap.containsKey("\"date\"") : "No \"date\" (timestamp) in the given event. Timestamp must be in event - and not trace or global attributes; events are atomic and therefore must have distinct timestamps";
            assert eventMap.containsKey("\"concept:name\"") : "No \"concept:name\" (activity) in the given event. Activity must be in event - and not in trace or global attributes; this would be ambiguous since caseID is also called \"concept:name\"";

            String caseID = maybeRemoveOuterQuotes(getCaseID(traceGlobalAttrs, traceAttributes));
            String activity = maybeRemoveOuterQuotes((String) eventMap.get("\"concept:name\""));
            String timestamp = maybeRemoveOuterQuotes((String) eventMap.get("\"date\""));
            Set<Attribute<?>> attributes = parseNonEssentialEventAttributes(traceGlobalAttrs, eventGlobalAttrs, traceAttributes, eventMap); // TODO: can be optimized; combine global and trace attributes in the loop, rather than in this method.
            trace.add(new Event(caseID, activity, timestamp, attributes));
        }
        return new Trace(trace);

    }


    private String getCaseID(Map<String, Object> globalAttributes, Map<String, Object> traceAttributes) {
        String caseID;
        String identifier = "\"concept:name\"";
        // fetch caseID from trace attributes before global attributes.
        caseID = (String) traceAttributes.get(identifier);
        if (caseID == null) { caseID = (String) globalAttributes.get(identifier); }
        assert caseID != null : "no caseID found";

        return caseID;
    }

    private String maybeRemoveOuterQuotes(String str) {
        if (Parsing.isWrapped(str, '\"', '\"')) {
            return Parsing.unWrap(str, '\"', '\"');
        }
        return str;
    }

    /** Parses attributes that are not the case ID, activity, and time stamp */
    private Set<Attribute<?>> parseNonEssentialEventAttributes(Map<String, Object> traceGlobalAttributes,
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



    private Pair<Map<String, Object>, Map<String, Object>> getTraceAndEventGlobalAttributes(Map<String, Object> jsonMap) {
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
