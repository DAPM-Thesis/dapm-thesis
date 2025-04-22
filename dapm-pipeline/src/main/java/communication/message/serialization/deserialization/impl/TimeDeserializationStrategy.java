package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Time;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.time.LocalDateTime;

public class TimeDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String s) {
        return new Time(LocalDateTime.parse(s));
    }
}
