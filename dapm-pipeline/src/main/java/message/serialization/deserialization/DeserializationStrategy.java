package message.serialization.deserialization;

import message.Message;

public interface DeserializationStrategy {
    Message deserialize(String payload);
}
