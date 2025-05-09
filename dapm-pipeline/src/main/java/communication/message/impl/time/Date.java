package communication.message.impl.time;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.impl.DateDeserializationStrategy;

import java.time.ZonedDateTime;

/** A date (time) including time zone - so it is unambiguous. */
public class Date extends Message {
    private final ZonedDateTime time;

    public Date(ZonedDateTime time) {
        super(new DateDeserializationStrategy());
        this.time = time;
    }
    
    public Date() {
        super(new DateDeserializationStrategy());
        time = ZonedDateTime.now();
    }

    public ZonedDateTime getTime() {
        return time;
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Date otherTime)) return false;
        return time.equals(otherTime.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }
}
