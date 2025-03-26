package message;

import message.serialization.MessageVisitor;
import message.serialization.deserialization.DeserializationStrategy;

public abstract class Message {
    protected DeserializationStrategy deserializationStrategy;

    protected Message(DeserializationStrategy strategy) { this.deserializationStrategy = strategy; }

    public DeserializationStrategy getDeserializationStrategy() { return deserializationStrategy; }

    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(MessageVisitor<?> v);
}
