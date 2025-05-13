package communication.message;

import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategy;
public abstract class Message {
    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(MessageVisitor<?> v);
}
