package draft_validation;

import java.util.Objects;
import java.util.Set;

public class MetadataChannel {
    private final MetadataProcessingElement producer;
    private final Set<MetadataConsumer> consumers;

    public MetadataChannel(MetadataProcessingElement producer, Set<MetadataConsumer> consumers) {
        this.producer = producer;
        this.consumers = consumers;
    }

    public MetadataChannel(MetadataProcessingElement producer, MetadataConsumer singleConsumer) {
        this.producer = producer;
        this.consumers = Set.of(singleConsumer);
    }

    @Override
    public String toString() {
        return "MC[" + producer + ", " + consumers + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MetadataChannel otherChannel)) return false;
        return producer.equals(otherChannel.producer) && consumers.equals(otherChannel.consumers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, consumers);
    }
}
