package draft_validation;

import communication.message.Message;

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
        return hasConsistentTypeAtPort(draft.channels())
                && consumerInputsMatchElementInputs(draft);
    }

    /** returns true iff the elements in draft all have all their inputs filled. */
    private static boolean consumerInputsMatchElementInputs(PipelineDraft draft) {
        // method: reverse engineer inputs from draft channels and validate that they match the inputs of the draft elements
        assert hasConsistentElements(draft);

        Map<MetadataProcessingElement, List<Class<? extends Message>>> inferredInputs = getInferredInputs(draft.channels());
        if (inferredInputs == null)
            { return false; }

        Set<MetadataProcessingElement> draftConsumers = draft.elements().stream().filter(element -> !element.isSource()).collect(Collectors.toSet());
        // inferred inputs match actual inputs for all elements
        return inferredInputs.keySet().equals(draftConsumers)
                && draftConsumers.stream().allMatch(element -> inferredInputs.get(element).equals(element.getInputs()));
    }

    /** returns A map with all the input channel's consumers as keys. The corresponding values is the inferred list of
     *  inputs for that element. returns null if a consumer's port is invalid or if the same consumer has multiple
     *  producers to the same port. */
    private static Map<MetadataProcessingElement, List<Class<? extends Message>>> getInferredInputs(Set<MetadataChannel> channels) {
        assert !channels.isEmpty() : "expects the channels of a pipeline draft";

        Map<MetadataProcessingElement, List<Class<? extends Message>>> inferredInputs = new HashMap<>();
        for (MetadataChannel channel : channels) {
            Class<? extends Message> producerOutput = channel.output();
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                int port = consumer.getPortNumber();
                MetadataProcessingElement consumerElement = consumer.getElement();
                if (!inferredInputs.containsKey(consumerElement)) {
                    nullInitializeConsumerInputs(consumerElement, inferredInputs);
                }

                List<Class<? extends Message>> inferredInputList = inferredInputs.get(consumerElement);
                if ((port < 0 || port >= inferredInputList.size()) // the consumer's port is out of bounds
                        || inferredInputList.get(port) != null) // element has multiple producers to the same port
                    { return null; }

                inferredInputs.get(consumerElement).set(port, producerOutput);
            }
        }

        assert extractConsumerElements(channels).equals(inferredInputs.keySet()) : "inferred inputs not set correctly";
        return inferredInputs;
    }

    private static void nullInitializeConsumerInputs(MetadataProcessingElement consumerElement, Map<MetadataProcessingElement, List<Class<? extends Message>>> inferredInputs) {
        int consumerInputCount = consumerElement.inputCount();
        List<Class<? extends Message>> nullFilledInputs = new ArrayList<>(Collections.nCopies(consumerInputCount, null));
        inferredInputs.put(consumerElement, nullFilledInputs);
    }

    private static boolean hasConsistentTypeAtPort(Set<MetadataChannel> channels) {
        for (MetadataChannel channel : channels) {
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                MetadataProcessingElement consumerElement = consumer.getElement();
                int port = consumer.getPortNumber();
                assert !consumerElement.isSource() : "Consumer must have at least one input.";

                Class<? extends Message> producerOutput = channel.output();
                if (port >= consumerElement.inputCount() || producerOutput != consumerElement.typeAt(port))
                    { return false; }
            }
        }
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
        return channel.getSubscribers().stream().map(MetadataSubscriber::getElement).collect(Collectors.toSet());
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
                .flatMap(channel -> channel.getSubscribers().stream())
                .map(MetadataSubscriber::getElement)
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
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                channelElements.add(consumer.getElement());
            }
        }

        return channelElements.equals(draft.elements());
    }

    private static boolean hasSource(Collection<MetadataProcessingElement> elements) {
        return !elements.isEmpty()
                && elements.stream().anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasSink(Collection<MetadataProcessingElement> elements) {
        return !elements.isEmpty()
                && elements.stream().anyMatch(MetadataProcessingElement::isSink);
    }


}
