package communication.message.serialization;

import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import utils.Pair;

import java.util.*;

public class JXESParsing {

    public static Map<String, Object> toJSONMap(String json) {
        return getContainer(json);
    }

    /** A string is wrapped iff the first non-whitespace character is start and the last non-whitespace character is
     *  end. */
    public static boolean isWrapped(String str, char start, char end) {
        int first = findNonWhitespaceIndex(str, 0, 1, start);
        if (first == -1) return false; // Start wrapper not found

        int last = findNonWhitespaceIndex(str, str.length() - 1, -1, end);
        return last != -1; // End wrapper found
    }

    public static String unWrap(String str, char startWrapper, int endWrapper) {
        int startIndex = str.indexOf(startWrapper);
        int endIndex = str.lastIndexOf(endWrapper);
        return str.substring(startIndex+1, endIndex);
    }

    public static String getCaseID(Map<String, Object> globalAttributes, Map<String, Object> traceAttributes) {
        String caseID;
        String identifier = "\"concept:name\"";
        // fetch caseID from trace attributes before global attributes.
        caseID = (String) traceAttributes.get(identifier);
        if (caseID == null) { caseID = (String) globalAttributes.get(identifier); }
        assert caseID != null : "no caseID found";

        return caseID;
    }

    public static String maybeRemoveOuterQuotes(String str) {
        if (JXESParsing.isWrapped(str, '\"', '\"')) {
            return JXESParsing.unWrap(str, '\"', '\"');
        }
        return str;
    }

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

    private static Attribute<?> parseAttribute(String name, Object value) {
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

    private static Object parseAttrValue(Object value) {
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

    private static Object getSimpleAttributeValue(String value) {
        assert !value.isEmpty() : "string may not be empty [but is allowed to contain empty quotation marks, i.e. '\"\"']";
        if (isStringValue(value)) { return JXESParsing.unWrap(value, '"', '"'); }
        else if (isBooleanValue(value)) {return Boolean.parseBoolean(value); }
        else if (isInteger(value)) { return Integer.parseInt(value); }
        else if (isDouble(value)) { return Double.parseDouble(value); }
        else { throw new IllegalStateException("Unsupported type: " + value); }
    }

    private static Map<String, Attribute<?>> getNestedAttributes(Map<String, Object> container) {
        Map<String, Attribute<?>> nestedAttributes = new HashMap<>();
        for (Map.Entry<String, Object> entry : container.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            nestedAttributes.put(name, parseAttribute(name, value));
        }
        return nestedAttributes;
    }

    private static boolean isNestedAttribute(Map<String, Object> map) {
        // "value" and "nested-attrs" are keywords reserved for nested attributes.
        return map.containsKey("\"value\"") && map.containsKey("\"nested-attrs\"");
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBooleanValue(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private static boolean isStringValue(String value) {
        return JXESParsing.isWrapped(value, '"', '"');
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

    private static Map<String, Object> getContainer(String container) {
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

    private static Object getStringValue(String valueStr) {
        return valueStr.strip();
    }


    /** returns the list of items in the given list ('[' and ']' wrapped string */
    private static List<Object> getList(String listStr) {
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

    private static Pair<String, String> splitAndStripKeyAndValue(String pair) {
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
    private static List<String> commaSplit(String contents) {
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

    private static boolean shouldFlipQuote(String str, int quoteIndex) {
        // the quotes should only be flipped if there are no quotes in the given string, or if the quotes in the given
        // string are closed. Both cases only happen if the number of backslashes in the string is even.
        int backslashCount = 0;
        while (--quoteIndex >= 0 && str.charAt(quoteIndex) == '\\') {backslashCount++;}
        return backslashCount % 2 == 0;
    }

    private static boolean isContainer(String str) {
        return isWrapped(str, '{', '}');
    }

    private static boolean isList(String str) {
        return isWrapped(str, '[', ']');
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
