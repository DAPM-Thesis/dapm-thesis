package candidate_validation;

import communication.message.Message;
import candidate_validation.parsing.InvalidCandidate;

import java.util.*;
import java.util.stream.Collectors;

public class PipelineCandidateValidator {
    public static boolean isValid(PipelineCandidate draft) throws InvalidCandidate {
        return hasConsistentElements(draft)
                && !isCyclic(draft)
                && isConnected(draft)
                && channelInputsAndOutputsMatch(draft)
                && allPathsFromSourceToSink(draft);
    }

    private static boolean allPathsFromSourceToSink(PipelineCandidate draft) {
        Set<ProcessingElementReference> elements = draft.getElements();
        Set<ChannelReference> channels = draft.getChannels();
        for (ProcessingElementReference element : elements) {
            if (element.isSource() && isSubscriber(element, channels)) { return false; }
            else if (element.isSink() && isPublisher(element, channels)) { return false; }
            else if (element.isOperator()
                    && !(isPublisher(element, channels) && isSubscriber(element, channels)) ) { return false; }
            else {
                if (!element.isSource() && !element.isSink() && !element.isOperator()) { throw new IllegalStateException("unexpected ProcessingElementReference type."); }
            }
        }
        return true;
    }

    private static boolean isSubscriber(ProcessingElementReference element, Set<ChannelReference> channels) {
        return channels.stream()
                .flatMap(channel -> channel.getSubscribers().stream())
                .anyMatch(subscriber -> subscriber.getElement().equals(element));
    }

    private static boolean isPublisher(ProcessingElementReference element, Set<ChannelReference> channels) {
        return channels.stream().anyMatch(channel -> channel.getProducer().equals(element));
    }

    private static boolean channelInputsAndOutputsMatch(PipelineCandidate draft) {
        return hasConsistentTypeAtPort(draft.getChannels())
                && consumerInputsMatchElementInputs(draft);
    }

    /** returns true iff the elements in draft all have all their inputs filled. */
    private static boolean consumerInputsMatchElementInputs(PipelineCandidate draft) {
        // method: reverse-engineer inputs from draft channels and validate that they match the inputs of the draft (subscriber) elements
        if (!hasConsistentElements(draft)) { throw new InvalidCandidate("The processing elements in \"channels\" must match those in \"processing elements\"."); }

        Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs = getInferredInputs(draft.getChannels());
        if (inferredInputs == null)
            { return false; }

        Set<ProcessingElementReference> draftConsumers = draft.getElements().stream().filter(element -> !element.isSource()).collect(Collectors.toSet());
        // inferred inputs match actual inputs for all elements
        return inferredInputs.keySet().equals(draftConsumers)
                && draftConsumers.stream().allMatch(element -> inferredInputs.get(element).equals(element.getInputs()));
    }

    /** Returns a Map with all the input channel's consumers as keys. The corresponding value is the inferred list of
     *  inputs for that element. Returns null if a consumer's port is invalid or if the same consumer has multiple
     *  producers to the same port. */
    private static Map<ProcessingElementReference, List<Class<? extends Message>>> getInferredInputs(Set<ChannelReference> channels) {
        Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs = new HashMap<>();
        for (ChannelReference channel : channels) {
            Class<? extends Message> producerOutput = channel.output();
            for (SubscriberReference subscriber : channel.getSubscribers()) {
                int port = subscriber.getPortNumber();
                ProcessingElementReference consumerElement = subscriber.getElement();
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

        if (!extractConsumerElements(channels).equals(inferredInputs.keySet())) {throw new IllegalStateException("inferred inputs not set correctly in this method implementation"); }
        return inferredInputs;
    }

    private static void nullInitializeConsumerInputs(ProcessingElementReference consumerElement, Map<ProcessingElementReference, List<Class<? extends Message>>> inferredInputs) {
        int consumerInputCount = consumerElement.inputCount();
        List<Class<? extends Message>> nullFilledInputs = new ArrayList<>(Collections.nCopies(consumerInputCount, null));
        inferredInputs.put(consumerElement, nullFilledInputs);
    }

    private static boolean hasConsistentTypeAtPort(Set<ChannelReference> channels) {
        for (ChannelReference channel : channels) {
            for (SubscriberReference subscriber : channel.getSubscribers()) {
                ProcessingElementReference element = subscriber.getElement();
                int port = subscriber.getPortNumber();

                Class<? extends Message> producerOutput = channel.output();
                if (port >= element.inputCount() || producerOutput != element.typeAt(port))
                    { return false; }
            }
        }
        return true;
    }


    private static boolean isConnected(PipelineCandidate draft) {
        if (!hasConsistentElements(draft)) { throw new InvalidCandidate("The processing elements in \"channels\" must match those in \"processing elements\"."); }

        List<ProcessingElementReference> orderedSources = getSources(draft).stream().toList();

        HashSet<ProcessingElementReference> visited =  new HashSet<>();
        Map<ProcessingElementReference, Set<ProcessingElementReference>> unDirectedSuccessors = getUndirectedSuccessors(draft.getChannels());
        depthFirstVisit(orderedSources.getFirst(), visited, unDirectedSuccessors);

        return visited.equals(draft.getElements());
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

    private static Set<ProcessingElementReference> getSources(PipelineCandidate draft) {
        return draft.getElements().stream().filter(ProcessingElementReference::isSource).collect(Collectors.toSet());
    }

    private static boolean isCyclic(PipelineCandidate draft) {
        // isCyclic() makes no assumptions about connectedness
        Set<ChannelReference> channels = draft.getChannels();
        if (!hasConsistentElements(draft)) { throw new InvalidCandidate("The processing elements in \"channels\" must match those in \"processing elements\"."); }
        Map<ProcessingElementReference, Set<ProcessingElementReference>> successors = getSuccessors(channels);
        Set<ProcessingElementReference> sources = getSources(draft);

        Set<ProcessingElementReference> potentiallyRecurring = new HashSet<>();
        Set<ProcessingElementReference> visited = new HashSet<>();

        // it is crucial for this cyclicity algorithm that is run in a synchronized way
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

        if (successors.get(current) == null)
            { return false; }

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
        return channel.getSubscribers().stream().map(SubscriberReference::getElement).collect(Collectors.toSet());
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

    private static Set<ProcessingElementReference> extractConsumerElements(Collection<ChannelReference> channels) {
        return channels.stream()
                .flatMap(channel -> channel.getSubscribers().stream())
                .map(SubscriberReference::getElement)
                .collect(Collectors.toSet());
    }

    /** Ensures that the processing elements in draft.elements() are exactly the same as in draft.channels() */
    private static boolean hasConsistentElements(PipelineCandidate draft) {
        Set<ProcessingElementReference> channelElements =  new HashSet<>();
        for (ChannelReference channel : draft.getChannels()) {
            channelElements.add(channel.getProducer());
            for (SubscriberReference consumer : channel.getSubscribers()) {
                channelElements.add(consumer.getElement());
            }
        }

        return channelElements.equals(draft.getElements());
    }

}
