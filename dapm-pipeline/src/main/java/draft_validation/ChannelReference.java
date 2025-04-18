package draft_validation;

import communication.message.Message;

import java.util.Objects;
import java.util.Set;

public class ChannelReference {
    private final ProcessingElementReference producer;
    private final Set<MetadataSubscriber> subscribers;

    public ChannelReference(ProcessingElementReference producer, Set<MetadataSubscriber> subscribers) {
        this.producer = producer;
        this.subscribers = subscribers;
    }

    public ChannelReference(ProcessingElementReference producer, MetadataSubscriber singleSubscriber) {
        this.producer = producer;
        this.subscribers = Set.of(singleSubscriber);
    }

    public ProcessingElementReference getProducer() { return producer; }

    public Set<MetadataSubscriber> getSubscribers() { return subscribers; }

    public Class<? extends Message> output() { return producer.getOutput(); }

    @Override
    public String toString() {
        return "MC[" + producer + ", " + subscribers + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ChannelReference otherChannel)) return false;
        return producer.equals(otherChannel.producer) && subscribers.equals(otherChannel.subscribers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, subscribers);
    }

}
