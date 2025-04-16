package communication.message.serialization.parsing;

import utils.Pair;

import java.util.*;

public class JSONParser {
    public Object parse(String json) {
        if (isJSONArray(json)) { return parseJSONArray(json); }
        else if (isJSONObject(json)) { return parseJSONObject(json); }

        throw new InvalidJSON("A JSON string must either start with an array or an object.");
    }

    List<Object> parseJSONArray(String array) {
        assert isJSONArray(array) : "Expected an array but received: " + array;

        List<String> stringItems = commaSplitArray(array);

        List<Object> items = new ArrayList<>();
        for (String item : stringItems) { items.add(parseValue(item)); }
        return items;
    }

    Object parseValue(String item) {
        if (isJSONObject(item)) { return parseJSONObject(item); }
        if (isJSONArray(item)) { return parseJSONArray(item); }
        else { return parseSimpleType(item); }
    }

    Map<String, Object> parseJSONObject(String object) {
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
    Object parseSimpleType(String item) {
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
    boolean isWrapped(String str, char start, char end) {
        int first = findNonWhitespaceIndex(str, 0, 1, start);
        if (first == -1) return false; // Start wrapper not found

        int last = findNonWhitespaceIndex(str, str.length() - 1, -1, end);
        return last != -1; // End wrapper found
    }

    String unwrap(String str, char startWrapper, int endWrapper) {
        assert str.length() >= 2;
        int startIndex = str.indexOf(startWrapper);
        int endIndex = str.lastIndexOf(endWrapper);
        assert startIndex != -1 && endIndex != -1;
        return str.substring(startIndex+1, endIndex);
    }

    boolean isJSONObject(String str) {
        return isWrapped(str, '{', '}');
    }

    boolean isJSONArray(String str) {
        return isWrapped(str, '[', ']');
    }

    List<String> commaSplitArray(String array) {
        assert isJSONArray(array) : "Expected an array but received: " + array;
        return commaSplit(unwrap(array, '[', ']'));
    }

    List<String> commaSplitObject(String object) {
        assert isJSONObject(object) : "Expected an object but received: " + object;
        return commaSplit(unwrap(object, '{', '}'));
    }

    /** @param contents An unwrapped array/container.
     *  @return The strings between outermost commas of the contents input. The strings will be stripped of whitespace, \n, \t,
     *  and \r in both ends. Outermost commas are commas that are not inside a string, object, or array inside the current object/array. */
    private List<String> commaSplit(String contents) {
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

    Pair<String, String> splitAndStripKeyAndValue(String pair) {
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
    private int findNonWhitespaceIndex(String str, int start, int step, char target) {
        for (int i = start; i >= 0 && i < str.length(); i += step) {
            char ch = str.charAt(i);
            if (ch == target) return i;
            if (!Character.isWhitespace(ch)) return -1;
        }
        return -1;
    }

    boolean isString(String value) {
        return isWrapped(value, '"', '"');
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

    private boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private boolean isNull(String value) {
        return value.equals("null");
    }
}
