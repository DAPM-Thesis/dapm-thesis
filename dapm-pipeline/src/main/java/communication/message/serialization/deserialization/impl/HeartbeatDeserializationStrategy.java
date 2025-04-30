package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Heartbeat;
import communication.message.impl.HeartbeatID;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.time.Instant;
import java.util.UUID;

public final class HeartbeatDeserializationStrategy implements DeserializationStrategy {
    @Override public Message deserialize(String payload) {
        String json = payload.trim();
        String id   = json.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*","$1");
        String ts   = json.replaceAll(".*\"ts\"\\s*:\\s*(\\d+).*","$1");
        return new Heartbeat(
            new HeartbeatID(UUID.fromString(id)), 
            Instant.ofEpochMilli(Long.parseLong(ts)));
    }
}
