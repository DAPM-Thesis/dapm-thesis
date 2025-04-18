package draft_validation;

import communication.message.Message;
import draft_validation.parsing.InvalidDraft;

import java.util.*;
import java.util.stream.Collectors;

public class PipelineValidator {
    public static boolean isValid(PipelineDraft draft) throws InvalidDraft {
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
        if (!hasConsistentElements(draft)) { throw new InvalidDraft("The processing elements in \"channels\" must match those in \"processing elements\"."); }

        Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs = getInferredInputs(draft.channels());
        if (inferredInputs == null)
            { return false; }

        Set<ProcessingElementReference> draftConsumers = draft.elements().stream().filter(element -> !element.isSource()).collect(Collectors.toSet());
        // inferred inputs match actual inputs for all elements
        return inferredInputs.keySet().equals(draftConsumers)
                && draftConsumers.stream().allMatch(element -> inferredInputs.get(element).equals(element.getInputs()));
    }

    /** returns A map with all the input channel's consumers as keys. The corresponding values is the inferred list of
     *  inputs for that element. returns null if a consumer's port is invalid or if the same consumer has multiple
     *  producers to the same port. */
    private static Map<ProcessingElementReference, List<Class<? extends Message>>> getInferredInputs(Set<ChannelReference> channels) {
        if (channels.isEmpty()){ throw new InvalidDraft("Expects the channels of a pipeline draft"); }

        Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs = new HashMap<>();
        for (ChannelReference channel : channels) {
            Class<? extends Message> producerOutput = channel.output();
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                int port = consumer.getPortNumber();
                ProcessingElementReference consumerElement = consumer.getElement();
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

        if (!extractConsumerElements(channels).equals(inferredInputs.keySet())) {throw new IllegalStateException("inferred inputs not set correctly in this method"); }
        return inferredInputs;
    }

    private static void nullInitializeConsumerInputs(ProcessingElementReference consumerElement, Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs) {
        int consumerInputCount = consumerElement.inputCount();
        List<Class<? extends Message>> nullFilledInputs = new ArrayList<>(Collections.nCopies(consumerInputCount, null));
        inferredInputs.put(consumerElement, nullFilledInputs);
    }

    private static boolean hasConsistentTypeAtPort(Set<ChannelReference> channels) {
        for (ChannelReference channel : channels) {
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                ProcessingElementReference consumerElement = consumer.getElement();
                int port = consumer.getPortNumber();
                if (consumerElement.isSource()) { throw new InvalidDraft("Consumer must have at least one input."); }

                Class<? extends Message> producerOutput = channel.output();
                if (port >= consumerElement.inputCount() || producerOutput != consumerElement.typeAt(port))
                    { return false; }
            }
        }
        return true;
    }


    private static boolean isConnected(PipelineDraft draft) {
        if (draft.elements().isEmpty()) {
            if (!draft.channels().isEmpty()){ throw new InvalidDraft("Channels over non-existing elements."); }
            return true;
        }
        if (!hasConsistentElements(draft)) { throw new InvalidDraft("The processing elements in \"channels\" must match those in \"processing elements\"."); }

        List<ProcessingElementReference> orderedSources = getSources(draft).stream().toList();
        if (orderedSources.isEmpty()) { throw new InvalidDraft("Pipeline Draft must contain at least 1 source"); }

        HashSet<ProcessingElementReference> visited =  new HashSet<>();
        Map<ProcessingElementReference, Set<ProcessingElementReference>> unDirectedSuccessors = getUndirectedSuccessors(draft.channels());
        depthFirstVisit(orderedSources.getFirst(), visited, unDirectedSuccessors);

        return visited.equals(draft.elements());
    }

    private static void depthFirstVisit(ProcessingElementReference current,
                                        HashSet<ProcessingElementReference> visited,
                                        Map<ProcessingElementReference, Set<ProcessingElementReference>> unDirectedSuccessors) {
        if (visited.contains(current)) { return; }
        visited.add(current);

        for (ProcessingElementReference successor : unDirectedSuccessors.getOrDefault(current, Collections.emptySet())) {
            depthFirstVisit(successor, visited, unDirectedSuccessors);
        }
    }

    private static Set<ProcessingElementReference> getSources(PipelineDraft draft) {
        if (hasConsumingSource(draft.channels())) { throw new InvalidDraft("The provided draft has a consuming source. All sources must have empty inputs."); }
        return draft.elements().stream().filter(ProcessingElementReference::isSource).collect(Collectors.toSet());
    }

    private static boolean isCyclic(PipelineDraft draft) {
        // isCyclic() makes no assumptions about connectedness
        Set<ChannelReference> channels = draft.channels();
        if (!hasConsistentElements(draft)) { throw new InvalidDraft("The processing elements in \"channels\" must match those in \"processing elements\"."); }
        Map<ProcessingElementReference, Set<ProcessingElementReference>> successors = getSuccessors(channels);
        Set<ProcessingElementReference> sources = getSources(draft);
        if (hasConsumingSource(channels)) { throw new InvalidDraft("The provided draft has a consuming source. All sources must have empty inputs."); }

        Set<ProcessingElementReference> potentiallyRecurring = new HashSet<>();
        Set<ProcessingElementReference> visited = new HashSet<>();

        // it is crucial for this cyclicity algorithm, that is run in a synchronized way
        for (ProcessingElementReference source : sources) {
            if (dfsCheckCycle(source, potentiallyRecurring, visited, successors))
                { return true; }
        }
        return false;
    }

    private static boolean dfsCheckCycle(final ProcessingElementReference current,
                                         Set<ProcessingElementReference> potentiallyRecurring,
                                         Set<ProcessingElementReference> visited,
                                         final Map<ProcessingElementReference, Set<ProcessingElementReference>> successors) {
        if (potentiallyRecurring.contains(current)) { return true; }
        if (visited.contains(current)) {  return false; }

        visited.add(current);

        if (successors.get(current) == null) {
            if (!current.isSink()) { throw new InvalidDraft("Any other element than a sink should have successors."); }
            return false;
        }

        if (successors.get(current).isEmpty()) { throw new InvalidDraft("Any other element than a sink should have successors."); }
        potentiallyRecurring.add(current);
        for (ProcessingElementReference successor : successors.get(current)) {
            if (dfsCheckCycle(successor, potentiallyRecurring, visited, successors))
                { return true;}
        }

        potentiallyRecurring.remove(current);
        return false;
    }

    private static Map<ProcessingElementReference, Set<ProcessingElementReference>> getSuccessors(Collection<ChannelReference> channels) {
        Map<ProcessingElementReference, Set<ProcessingElementReference>> successors = new HashMap<>();
        for (ChannelReference channel : channels) {
            ProcessingElementReference from = channel.getProducer();
            if (!successors.containsKey(from)) { successors.put(from, new HashSet<>()); }
            Set<ProcessingElementReference> consumingElements = extractConsumerElements(channel);
            successors.get(from).addAll(consumingElements);
        }

        return successors;
    }

    private static Set<ProcessingElementReference> extractConsumerElements(ChannelReference channel) {
        return channel.getSubscribers().stream().map(MetadataSubscriber::getElement).collect(Collectors.toSet());
    }

    private static Map<ProcessingElementReference, Set<ProcessingElementReference>> getUndirectedSuccessors(Set<ChannelReference> channels) {
        Map<ProcessingElementReference, Set<ProcessingElementReference>> successors = new HashMap<>();
        for (ChannelReference channel : channels) {
            ProcessingElementReference from = channel.getProducer();
            Set<ProcessingElementReference> consumerElements = extractConsumerElements(channel);
            successors.computeIfAbsent(from, k -> new HashSet<>()).addAll(consumerElements);
            for (ProcessingElementReference consumerElement : consumerElements) {
                successors.computeIfAbsent(consumerElement, k -> new HashSet<>()).add(from);
            }
        }

        return successors;
    }

    private static boolean hasConsumingSource(Collection<ChannelReference> channels) {
        return extractConsumerElements(channels).stream().anyMatch(ProcessingElementReference::isSource);
    }

    private static Set<ProcessingElementReference> extractConsumerElements(Collection<ChannelReference> channels) {
        return channels.stream()
                .flatMap(channel -> channel.getSubscribers().stream())
                .map(MetadataSubscriber::getElement)
                .collect(Collectors.toSet());
    }

    private static boolean hasProducingSink(Collection<ChannelReference> channels) {
        return channels.stream()
                .map(ChannelReference::getProducer)
                .anyMatch(ProcessingElementReference::isSink);
    }

    /** Ensures that the processing elements in draft.elements() are exactly the same as in draft.channels() */
    private static boolean hasConsistentElements(PipelineDraft draft) {
        Set<ProcessingElementReference> channelElements =  new HashSet<>();
        for (ChannelReference channel : draft.channels()) {
            channelElements.add(channel.getProducer());
            for (MetadataSubscriber consumer : channel.getSubscribers()) {
                channelElements.add(consumer.getElement());
            }
        }

        return channelElements.equals(draft.elements());
    }

    private static boolean hasSource(Collection<ProcessingElementReference> elements) {
        return !elements.isEmpty()
                && elements.stream().anyMatch(ProcessingElementReference::isSource);
    }

    private static boolean hasSink(Collection<ProcessingElementReference> elements) {
        return !elements.isEmpty()
                && elements.stream().anyMatch(ProcessingElementReference::isSink);
    }


}
