package communication.message.serialization.parsing;

import utils.Pair;

import java.util.*;

public class JSONParsing {
    public static Object parse(String json) {
        if (isJSONArray(json)) { return parseJSONArray(json); }
        else if (isJSONObject(json)) { return parseJSONObject(json); }

        throw new InvalidJSON("A JSON string must either start with an array or an object.");
    }

    static List<Object> parseJSONArray(String array) {
        assert isJSONArray(array) : "Expected an array but received: " + array;

        List<String> stringItems = commaSplitArray(array);

        List<Object> items = new ArrayList<>();
        for (String item : stringItems) { items.add(parseValue(item)); }
        return items;
    }

    private static Object parseValue(String item) {
        if (isJSONObject(item)) { return parseJSONObject(item); }
        if (isJSONArray(item)) { return parseJSONArray(item); }
        else { return parseSimpleType(item); }
    }

    static Map<String, Object> parseJSONObject(String object) {
        assert isJSONObject(object): "Expected an object but received: " + object;

        List<String> nameValuePairs = commaSplitObject(object);

        Map<String, Object> objectMap = new HashMap<>();
        for (String pair : nameValuePairs) {
            Pair<String, String> nameAndValue = splitAndStripKeyAndValue(pair);
            String name = unwrap(nameAndValue.getFirst(), '\"', '\"');
            Object value = parseValue(nameAndValue.getSecond());
            objectMap.put(name, value);
        }

        return objectMap;
    }

    /** Strips away all leading and trailing whitespace characters. */
    private static Object parseSimpleType(String item) {
        item = item.strip();
        assert !item.isEmpty() : "string may not be empty [but is allowed to contain empty quotation marks, i.e. '\"\"']";
        if (isString(item)) { return unwrap(item, '"', '"'); }
        else if (isBoolean(item)) {return Boolean.parseBoolean(item); }
        else if (isInteger(item)) { return Integer.parseInt(item); }
        else if (isDouble(item)) { return Double.parseDouble(item); }
        else if (isNull(item)) { return null; }

        throw new InvalidJSON("Unsupported item type: " + item);
    }

    /** A string is wrapped iff the first non-whitespace character is start and the last non-whitespace character is
     *  end. */
    public static boolean isWrapped(String str, char start, char end) {
        int first = findNonWhitespaceIndex(str, 0, 1, start);
        if (first == -1) return false; // Start wrapper not found

        int last = findNonWhitespaceIndex(str, str.length() - 1, -1, end);
        return last != -1; // End wrapper found
    }

    public static String unwrap(String str, char startWrapper, int endWrapper) {
        assert str.length() >= 2;
        int startIndex = str.indexOf(startWrapper);
        int endIndex = str.lastIndexOf(endWrapper);
        assert startIndex != -1 && endIndex != -1;
        return str.substring(startIndex+1, endIndex);
    }

    static boolean isJSONObject(String str) {
        return isWrapped(str, '{', '}');
    }

    static boolean isJSONArray(String str) {
        return isWrapped(str, '[', ']');
    }

    static List<String> commaSplitArray(String array) {
        assert isJSONArray(array) : "Expected an array but received: " + array;
        return commaSplit(unwrap(array, '[', ']'));
    }

    static List<String> commaSplitObject(String object) {
        assert isJSONObject(object) : "Expected an object but received: " + object;
        return commaSplit(unwrap(object, '{', '}'));
    }

    /** @param contents An unwrapped array/container.
     *  @return The strings between outermost commas of the contents input. The strings will be stripped of whitespace, \n, \t,
     *  and \r in both ends. Outermost commas are commas that are not inside a string, object, or array inside the current object/array. */
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

    static Pair<String, String> splitAndStripKeyAndValue(String pair) {
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

    private static boolean isString(String value) {
        return JXESParsing.isWrapped(value, '"', '"');
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

    private static boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private static boolean isNull(String value) {
        return value.equals("null");
    }

    // TODO: delete the divide [peace through extinction <3]
    // ____________________________ THE DIVIDE ___________________________________-

    /*
    public static String maybeRemoveOuterQuotes(String str) { // TODO: remove this - and actually analyze when removing outer quotes is necessary or not; this is too lazy+unsafe
        if (JXESParsing.isWrapped(str, '\"', '\"')) {
            return JXESParsing.unwrap(str, '\"', '\"');
        }
        return str;
    }

    protected static Attribute<?> parseAttribute(String name, Object value) {
        name = unwrap(name, '\"', '\"');

        if (value instanceof Map<?, ?>) {
            if (!isNestedAttribute((Map<String, Object>) value)) {
                // TODO: remove this (and potentially move it to JXESParsing); it is not conceptually part of JSONParsing
                // TODO: potentially by overriding this method in JXESParsing
                throw new IllegalStateException("I don't think we should ever have this?");
            }
            Map<String, Object> map = (Map<String, Object>) value;
            Object nestedAttrValue = parseAttrValue(map.get("\"value\""));
            Map<String, Attribute<?>> nestedAttrs = getNestedAttributes((Map<String, Object>) map.get("\"nested-attrs\""));
            return new Attribute<>(name, nestedAttrValue, nestedAttrs);
        } else if (value instanceof List<?>){
            List<Attribute<?>> listItems = new ArrayList<>();
            for (Object item  : (List<Object>) value) {
            }
            return new Attribute<>(name, listItems);
        }else {
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

    private static Object getStringValue(String valueStr) {
        return valueStr.strip();
    }
    */

}
