package draft_validation;

import pipeline.processingelement.ProcessingElement;

import java.util.*;

public class PipelineValidator {

    public static boolean isValid(Collection<MetadataProcessingElement> elements,
                                  Collection<MetadataChannel> channels) {
        return hasSource(elements)
                && hasSink(elements)
                && hasConsistentElements(elements, channels)
                && !hasProducingSink(channels)
                && !hasConsumingSource(channels)
                && !isCyclic(channels);
    }

    private static boolean isCyclic(Collection<MetadataChannel> channels) {
        Map<MetadataProcessingElement, Set<MetadataProcessingElement>> successors = new HashMap<>();
        for (MetadataChannel channel : channels) {
            MetadataProcessingElement from = channel.fromElement();
            if (!successors.containsKey(from)) { successors.put(from, new HashSet<>()); }
            successors.get(from).add(channel.toElement());
        }
        return true;
    }

    private static boolean hasConsumingSource(Collection<MetadataChannel> channels) {
        return channels.stream()
                .map(MetadataChannel::toElement)
                .anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasProducingSink(Collection<MetadataChannel> channels) {
        return channels.stream()
                .map(MetadataChannel::fromElement)
                .anyMatch(MetadataProcessingElement::isSink);
    }

    /** All processing elements in elements must also be in channels [due to connectedness] and all processing elements
     *  in channels must also be in elements [for consistency]. */
    private static boolean hasConsistentElements(Collection<MetadataProcessingElement> elements, Collection<MetadataChannel> channels) {
        Collection<MetadataProcessingElement> channelElements = new HashSet<>();
        for (MetadataChannel channel : channels) {
            channelElements.add(channel.fromElement());
            channelElements.add(channel.toElement());
        }

        return elements.containsAll(channelElements) && channelElements.containsAll(elements);
    }

    private static boolean hasSource(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSource);
    }

    private static boolean hasSink(Collection<MetadataProcessingElement> elements) {
        return elements.stream().anyMatch(MetadataProcessingElement::isSink);
    }
}
