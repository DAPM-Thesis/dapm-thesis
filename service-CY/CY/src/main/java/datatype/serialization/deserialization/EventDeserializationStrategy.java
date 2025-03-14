package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.event.Event;
import utils.Pair;
import datatype.event.Attribute;

import java.util.*;
import java.util.regex.Pattern;

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
        if (isWrapped(str, '\"', '\"')) {
            return unWrap(str, '\"', '\"');
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
        if (isStringValue(value)) { return unWrap(value, '"', '"'); }
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
        return isWrapped(value, '"', '"');
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


    /** Recursively parses a json-formatted String to extract a json-like Map.
     *  @param jsonStr a json-formatted String.
     *  @return A nested map representing the given json string; It has a tree-like structure where a non-leaf node is
     *  a Hashmap from String to Hashmap, and a leaf is either a List (e.g. of events [represented as HashMaps] in a
     *  trace) or a String. When decoded, a value that is supposed to be a string will be wrapped in quotation marks,
     *  whereas a non-string will not. Hence, the leaf "\"hi\"" is a String, but "5.0" is not [it is a float]. */
    private Map<String, Object> getJsonMap(String jsonStr) {
        return getContainer(jsonStr);
    }

    private Map<String, Object> getContainer(String container) {
        Object value;
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


    /** returns the list of items in the given list ('[' and ']' wrapped string */
    private List<Object> getList(String listStr) {
        List<Object> elements = new ArrayList<>();
        listStr = unWrap(listStr, '[', ']');
        List<String> stringElements = commaSplit(listStr);
        // list items can be either containers [e.g. events in a trace] or string values [e.g. in classifiers]
        for (String elem : stringElements) {
            if (isContainer(elem)) {
                elements.add(getContainer(elem));
            } else {
                assert !isList(elem) : "cannot be a list";
                elements.add(getStringValue(elem));
            }
        }
        return elements;
    }

    private Pair<String, String> splitAndStripKeyAndValue(String pair) {
        /* the pair is always of the form key:pair, where the key is a quotation-wrapped character sequence which may
         * contain its own ':' (colon). */
        // find the closing quotation mark
        int endQuoteIndex = pair.indexOf('\"');
        assert endQuoteIndex != -1 : String.format("no starting quotation in: %s", pair);
        endQuoteIndex = pair.indexOf('\"', endQuoteIndex+1);
        assert endQuoteIndex != -1 : String.format("no closing quotation in: %s", pair);
        int colonIndex = pair.indexOf(':', endQuoteIndex);

        String key = pair.substring(0, colonIndex).strip();
        String value = pair.substring(colonIndex+1).strip();
        return new Pair<>(key, value);
    }


    /** @param contents An unwrapped list/container.
     *  @return The strings between commas of the contents input. The strings will be stripped of whitespace, \n, \t,
     *  and \r in both ends. */
    private List<String> commaSplit(String contents) {
        // TODO: refactor
        List<String> commaSeparatedStrings = new ArrayList<>();
        if (contents.isEmpty()) { return commaSeparatedStrings; }
        // since contents can be nested [they can contain lists/containers/quotes], we must only split at the current level
        int openedCurly = 0;
        int openedSquare = 0;
        boolean openedQuote = false;
        int currentStart = 0;

        for (int i = 0; i < contents.length(); i++) {
            char ch = contents.charAt(i);

            if (ch == '"') {
                if (shouldFlipQuote(contents, i)) {
                    openedQuote = !openedQuote;
                }
            } else if (!openedQuote) {
                if (ch == ',' && openedCurly == 0 && openedSquare == 0) {
                    commaSeparatedStrings.add(contents.substring(currentStart, i));
                    currentStart = i+1;
                }
                else if (ch == '{') { openedCurly++; }
                else if (ch == '}') { openedCurly--; }
                else if (ch == '[') { openedSquare++; }
                else if (ch == ']') { openedSquare--; }
            }
        }
        // remember to add the last
        commaSeparatedStrings.add(contents.substring(currentStart));
        return commaSeparatedStrings;
    }

    private boolean shouldFlipQuote(String str, int quoteIndex) {
        // the quotes should only be flipped if there are no quotes in the given string, or if the quotes in the given
        // string are closed. Both cases only happen if the number of backslashes in the string is even.
        int backslashCount = 0;
        while (--quoteIndex >= 0 && str.charAt(quoteIndex) == '\\') {backslashCount++;}
        return backslashCount % 2 == 0;
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

    /** A string is wrapped iff the first non-whitespace character is start and the last non-whitespace character is
     *  end. */
    private boolean isWrapped(String str, char start, char end) {
        // TODO: refactor isWrapped
        int first = findNonWhitespaceIndex(str, 0, 1, start);
        if (first == -1) return false; // Start wrapper not found

        int last = findNonWhitespaceIndex(str, str.length() - 1, -1, end);
        return last != -1; // End wrapper found
    }

    /** @param str The string to be searched.
     *  @param start The index at which to start the search.
     *  @param step The step with which to (iteratively) search. step=1 is moving forward, and step=-1 is moving backward.
     *  @param target The looked for index
     *  @return returns the index in str that matches the first occurrence of target. If the search finds a
     *          non-whitespace character from start with step size 'step' before finding target, or if no target char
     *          is found, then it returns -1. */
    private static int findNonWhitespaceIndex(String str, int start, int step, char target) {
        for (int i = start; i >= 0 && i < str.length(); i += step) {
            char ch = str.charAt(i);
            if (ch == target) return i;
            if (!Character.isWhitespace(ch)) return -1;
        }
        return -1;
    }

}
