package communication.message.impl;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.impl.HeartbeatDeserializationStrategy;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a heartbeat message from a Processing Element (PE),
 * containing a unique identifier and a timestamp.
 */
public final class Heartbeat extends Message {

    private final HeartbeatID   id;
    private final Instant       timestamp;

    public Heartbeat(HeartbeatID id, Instant timestamp) {
        super(new HeartbeatDeserializationStrategy());
        this.id        = Objects.requireNonNull(id);
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    public HeartbeatID getId()     { return id;        }
    public Instant     getTime()   { return timestamp; }
    @Override public String getName() { return "Heartbeat"; }

    @Override public void acceptVisitor(MessageVisitor<?> v) { v.visit(this); }

    public String serialize() {
        return getName() + ":"
             + "{\"id\":\"" + id + "\",\"ts\":" + timestamp.toEpochMilli() + '}';
    }

    @Override public int hashCode() {
        return Objects.hash(id, timestamp);
    }

    @Override public boolean equals(Object o) {
        return o instanceof Heartbeat hb
            && id.equals(hb.id)
            && timestamp.equals(hb.timestamp);
    }
}
