package communication.message.impl;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.deserialization.impl.InstantTimeDeserializationStrategy;

import java.time.Instant;
// TODO: change name to UTCTime?
public class InstantTime extends Message {
    private final Instant time;

    public InstantTime() {
        super(new InstantTimeDeserializationStrategy());
        this.time = Instant.now();
    }

    public InstantTime(Instant instant) {
        super(new InstantTimeDeserializationStrategy());
        this.time = instant;
    }

    public Instant getTime() { return time; }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof InstantTime otherInstantTime)) return false;
        return time.equals(otherInstantTime.time);
    }

    @Override
    public int hashCode() { return time.hashCode(); }
}
