package communication.message.impl.time;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.impl.InstantTimeDeserializationStrategy;

import java.time.Instant;

/** An absolute UTC time (a single instantaneous, unambiguous point on the time-line). */
public class UTCTime extends Message {
    private final Instant time;

    public UTCTime() {
        super(new InstantTimeDeserializationStrategy());
        this.time = Instant.now();
    }

    public UTCTime(Instant instant) {
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
        if (!(other instanceof UTCTime otherUTCTime)) return false;
        return time.equals(otherUTCTime.time);
    }

    @Override
    public int hashCode() { return time.hashCode(); }
}
