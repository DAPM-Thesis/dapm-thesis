package communication.message.impl;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
 
/** 
 * Unique identifier for a PE's heartbeat 
 */
public final class HeartbeatID implements Serializable {

    private final UUID uuid;

    public HeartbeatID()            { this.uuid = UUID.randomUUID(); }
    public HeartbeatID(UUID uuid)   { this.uuid = uuid; }

    public UUID getUuid()           { return uuid; }

    @Override public String toString()      { return uuid.toString(); }
    @Override public int hashCode()         { return uuid.hashCode(); }
    @Override public boolean equals(Object o) {
        return o instanceof HeartbeatID other && Objects.equals(uuid, other.uuid);
    }
}
