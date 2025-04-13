package draft_validation;

import java.util.*;
import java.util.stream.Collectors;

public class PipelineValidator {
    public static boolean isValid(PipelineDraft draft) {
        return hasConsistentElements(draft)
                && hasSource(draft.elements())
                && hasSink(draft.elements())
                && !hasProducingSink(draft.channels())
                && !hasConsumingSource(draft.channels())
                && !isCyclic(draft)
                && isConnected(draft)
                && channelInputsAndOutputsMatch(draft);
    }

    private static boolean channelInputsAndOutputsMatch(PipelineDraft draft) {
        /*
        Map<MetadataProcessingElement, List<Class<? extends Message>>> inputs = new HashMap<>();
        elements.forEach(e -> inputs.put(e, e.getInputs()));

        // TODO: It seems difficult to figure out what the type of a channel is. Consider using dynamic programming
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.getProducer();
            MetadataProcessingElement to = channel.toElement();
        }
        */
        return true;
    }


    private static boolean isConnected(PipelineDraft draft) {
        if (draft.elements().isEmpty()) {
            assert draft.channels().isEmpty() : "Channels over non-existing elements.";
            return true;
        }
        assert hasConsistentElements(draft);

        List<MetadataProcessingElement> orderedSources = getSources(draft).stream().toList();
        assert !orderedSources.isEmpty();

        HashSet<MetadataProcessingElement> visited =  new HashSet<>();
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> unDirectedSuccessors = getUndirectedSuccessors(draft.channels());
        depthFirstVisit(orderedSources.getFirst(), visited, unDirectedSuccessors);

        return visited.equals(draft.elements());
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

    private static Set<MetadataProcessingElement> getSources(PipelineDraft draft) {
        assert !hasConsumingSource(draft.channels());
        return draft.elements().stream().filter(MetadataProcessingElement::isSource).collect(Collectors.toSet());
    }

    private static boolean isCyclic(PipelineDraft draft) {
        // isCyclic() makes no assumptions about connectedness
        Set<MetadataChannel> channels = draft.channels();
        assert hasConsistentElements(draft)
                : "Assumes all processing elements in channels are in the elements set and vice versa.";
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = getSuccessors(channels);
        Set<MetadataProcessingElement> sources = getSources(draft);
        assert !hasConsumingSource(channels) : "Sources are not consumers";

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
            Set<MetadataProcessingElement> consumingElements = extractConsumerElements(channel);
            successors.get(from).addAll(consumingElements);
        }

        return successors;
    }

    private static Set<MetadataProcessingElement> extractConsumerElements(MetadataChannel channel) {
        return channel.getConsumers().stream().map(MetadataConsumer::getElement).collect(Collectors.toSet());
    }

    private static Map<MetadataProcessingElement, Set<MetadataProcessingElement>> getUndirectedSuccessors(Set<MetadataChannel> channels) {
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = new HashMap<>();
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.getProducer();
            Set<MetadataProcessingElement> consumerElements = extractConsumerElements(channel);
            successors.computeIfAbsent(from, k -> new HashSet<>()).addAll(consumerElements);
            for (MetadataProcessingElement consumerElement : consumerElements) {
                successors.computeIfAbsent(consumerElement, k -> new HashSet<>()).add(from);
            }
        }

        return successors;
    }

    private static boolean hasConsumingSource(Collection<MetadataChannel> channels) {
        return extractConsumerElements(channels).stream().anyMatch(MetadataProcessingElement::isSource);
    }

    private static Set<MetadataProcessingElement> extractConsumerElements(Collection<MetadataChannel> channels) {
        return channels.stream()
                .flatMap(channel -> channel.getConsumers().stream())
                .map(MetadataConsumer::getElement)
                .collect(Collectors.toSet());
    }

    private static boolean hasProducingSink(Collection<MetadataChannel> channels) {
        return channels.stream()
                .map(MetadataChannel::getProducer)
                .anyMatch(MetadataProcessingElement::isSink);
    }

    /** Ensures that the processing elements in draft.elements() are exactly the same as in draft.channels() */
    private static boolean hasConsistentElements(PipelineDraft draft) {
        Set<MetadataProcessingElement> channelElements =  new HashSet<>();
        for (MetadataChannel channel : draft.channels()) {
            channelElements.add(channel.getProducer());
            for (MetadataConsumer consumer : channel.getConsumers()) {
                channelElements.add(consumer.getElement());
            }
        }

        return channelElements.equals(draft.elements());
    }

    private static boolean hasSource(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasSink(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSink);
    }


}
