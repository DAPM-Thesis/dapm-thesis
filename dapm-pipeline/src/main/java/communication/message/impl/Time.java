package communication.message.impl;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.impl.TimeDeserializationStrategy;

import java.time.LocalDateTime;
import java.util.Objects;

public class Time extends Message {
    private final LocalDateTime time;

    public Time(LocalDateTime time) {
        super(new TimeDeserializationStrategy());
        this.time = time;
    }
    
    public Time() {
        super(new TimeDeserializationStrategy());
        time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Time otherTime)) return false;
        return time.equals(otherTime.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }
}
