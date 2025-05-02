package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.InstantTime;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.time.Instant;

public class InstantTimeDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        return new InstantTime(Instant.parse(payload));
    }
}
