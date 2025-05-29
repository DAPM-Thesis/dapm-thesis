package communication.message.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.HeartbeatDeserializationStrategy;
import communication.message.serialization.parsing.JSONParser;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = HeartbeatDeserializationStrategy.class)
public class Heartbeat extends Message {
    private final String instanceID;
    private final Instant timestamp;

    public Heartbeat(String instanceID, Instant timestamp) {
        this.instanceID = Objects.requireNonNull(instanceID, "instanceID cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    public String getInstanceID() {
        return instanceID;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }
    
    public String getPayloadAsJson() {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("instanceID", instanceID);
        payloadMap.put("timestamp", timestamp.toString());
        return JSONParser.toJSONString(payloadMap);
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
               "instanceID='" + instanceID + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Heartbeat that = (Heartbeat) o;
        return Objects.equals(instanceID, that.instanceID) &&
               Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceID, timestamp);
    }
}
