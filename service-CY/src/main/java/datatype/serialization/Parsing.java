package datatype.serialization;

import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parsing {

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
