package draft_validation;

import communication.message.Message;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipelineValidator {
    public static boolean isValid(Set<MetadataProcessingElement> elements,
                                  Set<MetadataChannel> channels) {
        return true;
    }
    /*
    public static boolean isValid(Set<MetadataProcessingElement> elements,
                                  Set<MetadataChannel> channels) {
        return hasConsistentElements(elements, channels)
                && hasSource(elements)
                && hasSink(elements)
                && !hasProducingSink(channels)
                && !hasConsumingSource(channels)
                && !isCyclic(elements, channels)
                && isConnected(elements, channels)
                && channelInputsAndOutputsMatch(elements, channels);
    }

    private static boolean channelInputsAndOutputsMatch(Set<MetadataProcessingElement> elements, Set<MetadataChannel> channels) {
        Map<MetadataProcessingElement, List<Class<? extends Message>>> inputs = new HashMap<>();
        elements.forEach(e -> inputs.put(e, e.getInputs()));

        // TODO: It seems difficult to figure out what the type of a channel is. Consider using dynamic programming
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.getProducer();
            MetadataProcessingElement to = channel.toElement();
        }

        return false;
    }


    private static boolean isConnected(Set<MetadataProcessingElement> elements, Set<MetadataChannel> channels) {
        if (elements.isEmpty()) {
            assert channels.isEmpty() : "Channels over non-existing elements.";
            return true;
        }
        assert hasConsistentElements(elements, channels);

        List<MetadataProcessingElement> orderedSources = getSources(elements).stream().toList();
        assert !orderedSources.isEmpty();

        HashSet<MetadataProcessingElement> visited =  new HashSet<>();
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> unDirectedSuccessors = getUndirectedSuccessors(channels);
        depthFirstVisit(orderedSources.getFirst(), visited, unDirectedSuccessors);

        return visited.equals(elements);
    }

    private static void depthFirstVisit(MetadataProcessingElement current,
                                        HashSet<MetadataProcessingElement> visited,
                                        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> unDirectedSuccessors) {
        if (visited.contains(current)) { return; }
        visited.add(current);

        for (MetadataProcessingElement successor : unDirectedSuccessors.getOrDefault(current, Collections.emptySet())) {
            depthFirstVisit(successor, visited, unDirectedSuccessors);
        }
    }

    private static Set<MetadataProcessingElement> getSources(Collection<MetadataProcessingElement> elements) {
        return elements.stream().filter(MetadataProcessingElement::isSource).collect(Collectors.toSet());
    }

    private static boolean isCyclic(Set<MetadataProcessingElement> elements, Set<MetadataChannel> channels) {
        // isCyclic() makes no assumptions about connectedness.
        assert hasConsistentElements(elements, channels)
                : "Assumes all processing elements in channels are in the elements set and vice versa.";
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = getSuccessors(channels);
        Set<MetadataProcessingElement> sources = getSources(elements);
        assert channels.stream().map(MetadataChannel::toElement).noneMatch(sources::contains) : "Sources are not consumers.";

        Set<MetadataProcessingElement> potentiallyRecurring = new HashSet<>();
        Set<MetadataProcessingElement> visited = new HashSet<>();

        // it is crucial for this cyclicity algorithm, that is run in a synchronized way
        for (MetadataProcessingElement source : sources) {
            if (dfsCheckCycle(source, potentiallyRecurring, visited, successors))
                { return true; }
        }
        return false;
    }

    private static boolean dfsCheckCycle(final MetadataProcessingElement current,
                                         Set<MetadataProcessingElement> potentiallyRecurring,
                                         Set<MetadataProcessingElement> visited,
                                         final Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors) {
        if (potentiallyRecurring.contains(current)) { return true; }
        if (visited.contains(current)) {  return false; }

        visited.add(current);

        if (successors.get(current) == null) {
            assert current.isSink() : "Any other element than a sink should have successors.";
            return false;
        }

        assert !successors.get(current).isEmpty() : "Any other element than a sink should have successors.";
        potentiallyRecurring.add(current);
        for (MetadataProcessingElement successor : successors.get(current)) {
            if (dfsCheckCycle(successor, potentiallyRecurring, visited, successors))
                { return true;}
        }

        potentiallyRecurring.remove(current);
        return false;
    }

    private static Map<MetadataProcessingElement, Set<MetadataProcessingElement>> getSuccessors(Collection<MetadataChannel> channels) {
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = new HashMap<>();
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.getProducer();
            if (!successors.containsKey(from)) { successors.put(from, new HashSet<>()); }

            successors.get(from).add(channel.toElement());
        }

        return successors;
    }

    private static Map<MetadataProcessingElement, Set<MetadataProcessingElement>> getUndirectedSuccessors(Set<MetadataChannel> channels) {
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = new HashMap<>();
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.getProducer();
            MetadataProcessingElement to = channel.toElement();
            if (!successors.containsKey(from)) { successors.put(from, new HashSet<>()); }
            if (!successors.containsKey(to)) { successors.put(to, new HashSet<>()); }
            successors.get(from).add(to);
            successors.get(to).add(from);
        }

        return successors;
    }

    private static boolean hasConsumingSource(Collection<MetadataChannel> channels) {
        return channels.stream()
                .map(MetadataChannel::toElement)
                .anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasProducingSink(Collection<MetadataChannel> channels) {
        return channels.stream()
                .map(MetadataChannel::getProducer)
                .anyMatch(MetadataProcessingElement::isSink);
    }

    private static boolean hasConsistentElements(Collection<MetadataProcessingElement> elements,
                                                 Collection<MetadataChannel> channels) {
        return channels.stream()
                .flatMap(channel -> Stream.of(channel.getProducer(), channel.toElement()))
                .collect(Collectors.toSet())
                .equals(elements);
    }

    private static boolean hasSource(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasSink(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSink);
    }

     */
}
