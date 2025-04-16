package communication.message.serialization.parsing;

import communication.message.impl.Trace;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import utils.Pair;

import java.util.*;

public class JXESParser extends JSONParser {

    /** returns a JXES map. It must contain the */
    @Override
    public Map<String, Object> parse(String jxes) {
        if (!(isJSONObject(jxes))) {
            throw new InvalidJXES("JXES string must start with an object, but received: " + jxes);
        }

        Map<String, String> outermostProperties = singleIterationObjectParse(jxes);
        if (!outermostProperties.containsKey("traces")) {
            throw new InvalidJXES("JXES string must contain \"traces\" key. Given: " + outermostProperties);
        }

        Map<String, Object> jxesMap = new HashMap<>();
        setObjectFromKey("log-properties", outermostProperties, jxesMap);
        setObjectFromKey("log-attrs", outermostProperties, jxesMap);
        setObjectFromKey("classifiers", outermostProperties, jxesMap);

        setExtensions(outermostProperties, jxesMap);
        setGlobalAttributes(outermostProperties, jxesMap);
        setTraces(outermostProperties, jxesMap);

        return jxesMap;
    }

    private void setTraces(Map<String, String> outermostProperties, Map<String, Object> jxesMap) {
        String rawTraces = outermostProperties.get("traces");
        if (rawTraces == null | !isJSONArray(rawTraces)) {
            throw new InvalidJXES("Provided traces is empty or improperly formatted. Should be a list, received: " + rawTraces);
        }

        List<String> splitTraces = commaSplitArray(rawTraces);
        if (splitTraces.isEmpty()) {
            throw new InvalidJXES("A JXES must include at least one trace. Received an empty list.");
        }

        List<Trace> traces = new ArrayList<>();
        splitTraces.forEach(traceStr -> traces.add(parseTrace(traceStr, jxesMap)));
        jxesMap.put("traces", traces);
    }

    private Trace parseTrace(String traceStr, Map<String, Object> jxesMap) {
        if (!isJSONObject(traceStr)) {
            throw new InvalidJXES("Expected a trace, but received: " + traceStr);
        }

        Map<String, String> singlyParsedTrace = singleIterationObjectParse(traceStr);
        Set<String> allowedAttributes = new HashSet<>(Set.of("attrs", "events"));
        if (!allowedAttributes.containsAll(singlyParsedTrace.keySet())
                || ! singlyParsedTrace.containsKey("events")) {
            throw new InvalidJXES("A Trace must contain the \"events\" key, and can optionally contain the \"attrs\" key. No other keys are allowed. Received " + singlyParsedTrace.keySet());
        }

        Map<String, Object> traceAttributes = getTraceAttributes(jxesMap, singlyParsedTrace);

        if (!traceAttributes.containsKey("concept:name")) {
            throw new InvalidJXES("The trace must have the \"concept:name\" attribute either in its 'attrs' or in 'global-attrs'->'trace'. The key was not found");
        }

        String eventsValue = singlyParsedTrace.get("events");
        return new Trace(parseEvents(eventsValue, jxesMap, traceAttributes));
    }

    private List<Event> parseEvents(String eventsStr, Map<String, Object> jxesMap, Map<String, Object> traceAttributes) {
        if (!isJSONArray(eventsStr)) {
            throw new InvalidJXES("Expected a JSON array of events but received: " + eventsStr);
        }

        Map<String, Object> globalEventAttributes = getGlobalEventAttributes(jxesMap);
        List<String> splitEvents = commaSplitArray(eventsStr);
        List<Event> events = new ArrayList<>();
        splitEvents.forEach(eventObject -> events.add(parseEvent(eventObject, globalEventAttributes, traceAttributes)));
        return events;
    }

    private Event parseEvent(String eventObject, Map<String, Object> globalEventAttributes, Map<String, Object> traceAttributes) {
        if (!isJSONObject(eventObject)) {
            throw new InvalidJXES("Expected an event JSON object but received: " + eventObject);
        }

        String caseID = (String) traceAttributes.get("concept:name");

        // make a copy of traceAttributes so we can remove the "concept:name" (representing caseID) key from trace.
        // This way we ensure that the "concept:name" key in the combinedAttributes represents the event activity.
        Map<String, Object> traceAttributesCopy = new HashMap<>(traceAttributes);
        traceAttributesCopy.remove("concept:name");

        // combine all attributes, with decreasing priority: event local > event global > trace local > trace global
        Map<String, Object> combinedAttributes = new HashMap<>(traceAttributesCopy);
        combinedAttributes.putAll(globalEventAttributes);
        combinedAttributes.putAll(parseJSONObject(eventObject));

        if (!combinedAttributes.containsKey("date") || !combinedAttributes.containsKey("concept:name")) {
            throw new InvalidJXES("An event must have a timestamp and an activity set, in the keys \"date\" and \"concept:name\", respectively.");
        }

        String activity = (String) combinedAttributes.remove("concept:name");
        String timestamp = (String) combinedAttributes.remove("date");
        Set<Attribute<?>> nonEssentialEventAttributes = parseNonEssentialEventAttributes(combinedAttributes);

        return new Event(caseID, activity, timestamp, nonEssentialEventAttributes);
    }

    private Set<Attribute<?>> parseNonEssentialEventAttributes(Map<String, Object> combinedNonEssentialEventAttributes) {
        if (!Collections.disjoint(combinedNonEssentialEventAttributes.keySet(),
                                  new HashSet<>(Set.of("concept:name", "date")))) {
            throw new InvalidJXES("expected non-essential attributes only, but received either concept:name or date");
        }

        Set<Attribute<?>> attributes = new HashSet<>();
        for (Map.Entry<String, Object> keyAndValue : combinedNonEssentialEventAttributes.entrySet()) {
            attributes.add(new Attribute<>(keyAndValue.getKey(), keyAndValue.getValue()));
        }

        return attributes;
    }

    private Map<String, Object> getGlobalEventAttributes(Map<String, Object> jxesMap) {
        if (jxesMap.containsKey("global-attrs")) {
            Map<String, Map<String, Object>> globalAttributes = (Map<String, Map<String, Object>>) jxesMap.get("global-attrs");
            if (globalAttributes.containsKey("event")) {
                return globalAttributes.get("event");
            }
        }

        return new HashMap<>();
    }

    /** This method collects both local and global attributes of the input trace string. When  */
    private Map<String, Object> getTraceAttributes(Map<String, Object> jxesMap, Map<String, String> singlyParsedTrace) {

        Map<String, Object> traceAttributes = new HashMap<>();
        Map<String, Map<String, Object>> globalTraceAttributes = (Map<String, Map<String, Object>>) jxesMap.get("global-attrs");
        if (globalTraceAttributes != null && globalTraceAttributes.containsKey("trace")) {
            traceAttributes.putAll(globalTraceAttributes.get("trace"));
        }
        if (singlyParsedTrace.containsKey("attrs")) {
            Map<String, Object> localAttributes = parseJSONObject(singlyParsedTrace.get("attrs"));
            traceAttributes.putAll(localAttributes);
        }
        return traceAttributes;
    }

    private void setObjectFromKey(String key, Map<String, String> outermostProperties, Map<String, Object> jxesMap) {
        String value = outermostProperties.get(key);
        if (value == null)
            { return; }
        jxesMap.put(key, parseJSONObject(value));
    }

    private void setExtensions(Map<String, String> outermostProperties, Map<String, Object> jxesMap) {
        String value = outermostProperties.get("extensions");
        if (value == null)
            { return; }
        jxesMap.put("extensions", parseJSONArray(value));
    }

    private void setGlobalAttributes(Map<String, String> outermostProperties, Map<String, Object> jxesMap) {
        String value = outermostProperties.get("global-attrs");
        if (value == null)
            { return; }

        Map<String, Object> parsedGlobalAttrs = parseJSONObject(value);
        Set<String> allowedKeys = new HashSet<>(Set.of("trace", "event"));
        if (!allowedKeys.containsAll(parsedGlobalAttrs.keySet())) {
            throw new InvalidJXES("Unexpected keys in JXES global attributes. Only \"trace\" and \"event\" expected, but received: " + parsedGlobalAttrs.keySet());
        }

        jxesMap.put("global-attrs", parsedGlobalAttrs);
    }

    /** takes as input a JSON object string and returns a map with the object's keys as its keys. The values
     *  are the (unparsed) string values of the input JSON object. */
    private Map<String, String> singleIterationObjectParse(String object) {
        assert isJSONObject(object) : "Expected a JSON object but received " + object;

        Map<String, String> singlyIteratedObject = new HashMap<>();
        List<String> keyValuePairs = commaSplitObject(object);
        for (String pair : keyValuePairs) {
            Pair<String, String> nameAndValue = splitAndStripKeyAndValue(pair);
            String name = unwrap(nameAndValue.getFirst(), '\"', '\"');
            singlyIteratedObject.put(name, nameAndValue.getSecond());
        }
        return singlyIteratedObject;
    }

    @Override
    Object parseValue(String item) {
        if (isJSONObject(item)) { return parseJSONObject(item); }
        if (isJSONArray(item)) { return parseJSONArray(item); }
        if (isNestedAttribute(item)) { return parseNestedAttribute(item); }
        else { return parseSimpleType(item); }
    }

    private Object parseNestedAttribute(String item) {
        return null;
    }

    private boolean isNestedAttribute(String item) {
        if (!isJSONObject(item))
            { return false; }
        Map<String, Object> maybeNestedAttribute = parseJSONObject(item);
        return maybeNestedAttribute.keySet().equals(Set.of("value", "nested-attrs"));
    }
}
