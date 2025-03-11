package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.event.Attribute;
import datatype.event.NestedAttribute;
import utils.Pair;

import java.util.*;

public class EventDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes a JXES-formatted string into an event. Assumes the string contains only a single event. */
    /*
    @Override
    public DataType deserialize(String payload) {
        // extract global attributes
        Map<String, Object> globalAttributes = new HashMap<>();
        String globalAttrsStr = "\"global-attrs\":";
        int globalAttrsIndex = payload.indexOf(globalAttrsStr);
        Pair<String, Integer> globalAttrsContentsAndEnd =
                getNextCurlyBracketContent(payload, globalAttrsIndex+globalAttrsStr.length());

        String contents = globalAttrsContentsAndEnd.getFirst();
        List<String> containedAttributes = getContainerAttributes(contents);
        // contains keys "event" and/or "trace" and their (potentially nested) values.
        for (String attr : containedAttributes) {
            String[] keyValue = attr.split(":", 2);
            String value = keyValue[1];
            Pair<String, Integer> valueAndEnd = getNextCurlyBracketContent(value);
            String valueContents = valueAndEnd.getFirst();
            List<String> valueAttributes =
        }

        return new Event("not implemented", "not implemented", "not implemented", new HashSet<>());
    }
    */

    @Override
    public DataType deserialize(String payload) {
        Attribute attributes = getAttributes(payload);
        return null;
    }

    /** recursively extracts the attributes of the input string */
    private Attribute getAttributes(String input) {
        if (isNested(input)) {
            NestedAttribute attributes = new NestedAttribute();
            // extract all key:value pairs at the current level
            List<String> keyValuePairs = commaSplitContent(input);
            for (String pair : keyValuePairs) {
                String[] keyAndValue = pair.split(":", 2);
                // recursively call getAttributes on all pairs
                String name = keyAndValue[0];
                Attribute value = getAttributes(keyAndValue[1]);
                attributes.put(name, value);
            }
            return attributes;

        } else if (isList(input)) {
            List<String> nestedAttributes = commaSplitContent(input);
            for (String pair : nestedAttributes) {

            }
        }

        throw new IllegalStateException("every option above must return");
    }


    private boolean isNested(String str) {
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

    private Pair<String, Integer> getNextCurlyBracketContent(String str)
        { return getNextCurlyBracketContent(str, 0);}

    /** Gets the content of the next matching curly bracket pair ('{' and '}'). */
    private Pair<String, Integer> getNextCurlyBracketContent(String str, int startIndex) {
        /* This method has O(n) time complexity for str.length() = n, and iterates the string at most twice: once to
           find a matching '{' and '}' pair, and once to create a substring with the contents.
         */
        int startBracketIndex = str.indexOf('{',startIndex);
        assert startBracketIndex != -1 : "No '{' found";

        // since there can be containers within containers one must ensure the matching '{' and '}' pair is found
        int openCount = 1; // start at 1 since '{' was found at startBracketIndex
        int current = startBracketIndex+1;
        while (openCount > 0 && current < str.length()) {
            char ch = str.charAt(current);
            if (ch == '{') {openCount++;}
            else if (ch == '}') {openCount--;}
            current++;
        }

        assert openCount == 0 && current != str.length() : "no matching '}' found";
        String contents = str.substring(startBracketIndex+1, current);
        return new Pair<>(contents, current+1);
    }

    /** @param containerContents The contents inside a container, without the container's encapsulating '{' and '}' [but
     *  may still contain '{' and '}' if a value inside the current container is another container].
     *  @return a list of all key:value pairs inside the given container's scope; a value may be a container. */
    private List<String> getContainerAttributes(String containerContents) {
        /* This method is O(n) for containerContents.length() = n, and will at most iterate the string twice:
        *  once to go over the contents while looking for commas, and once [combined] in creating substrings. */
        List<String> attributes = new ArrayList<>();
        int currentStart = 0;
        int current = 0;
        int openCount = 0;
        while (current < containerContents.length()) {
            char ch = containerContents.charAt(current);
            if (ch == '{') {openCount++;}
            else if (ch == '}') {
                openCount--;
                assert openCount >= 0: "bracket '}' with no prior '{'";}
            else if (ch == ',') {
                attributes.add(containerContents.substring(currentStart, current-1));
                currentStart = current+1;
            }
            current++;
        }
        attributes.add(containerContents.substring(currentStart, current));

        return attributes;
    }

}
