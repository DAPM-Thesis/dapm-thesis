package communication.message;

import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategy;
// TODO: make a guide on how to make a Message subclass (make DeserializationStrategy, add visitor, add to message_type schema, etc.)
public abstract class Message {
    protected DeserializationStrategy deserializationStrategy;
    // TODO: optimize deserializationstrategy such that each new instance does not have to create a DeserializationStrategy
    // TODO: e.g. by making deserializationStrategy static in the inheritors, by making an annotation, or something else...
    protected Message(DeserializationStrategy strategy) {
        this.deserializationStrategy = strategy;
    }

    public DeserializationStrategy getDeserializationStrategy() { return deserializationStrategy; }

    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(MessageVisitor<?> v);
}
