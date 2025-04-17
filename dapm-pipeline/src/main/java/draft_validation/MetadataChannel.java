package draft_validation;

import communication.message.Message;

import java.util.Objects;
import java.util.Set;

public class MetadataChannel {
    private final MetadataProcessingElement producer;
    private final Set<MetadataSubscriber> subscribers;

    public MetadataChannel(MetadataProcessingElement producer, Set<MetadataSubscriber> subscribers) {
        this.producer = producer;
        this.subscribers = subscribers;
    }

    public MetadataChannel(MetadataProcessingElement producer, MetadataSubscriber singleSubscriber) {
        this.producer = producer;
        this.subscribers = Set.of(singleSubscriber);
    }

    public MetadataProcessingElement getProducer() { return producer; }

    public Set<MetadataSubscriber> getSubscribers() { return subscribers; }

    public Class<? extends Message> output() { return producer.getOutput(); }

    @Override
    public String toString() {
        return "MC[" + producer + ", " + subscribers + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MetadataChannel otherChannel)) return false;
        return producer.equals(otherChannel.producer) && subscribers.equals(otherChannel.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, subscribers);
    }

}
