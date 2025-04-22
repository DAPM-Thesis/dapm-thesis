package communication.message.impl;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.impl.TimeDeserializationStrategy;

import java.time.LocalDateTime;
import java.util.Objects;

public class Time extends Message {
    LocalDateTime time;

    public Time(LocalDateTime time) {
        super(new TimeDeserializationStrategy());
        this.time = time;
    }
    
    public Time() {
        super(new TimeDeserializationStrategy());
        time = LocalDateTime.now();
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time otherTime)) return false;
        return time.equals(otherTime.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    public LocalDateTime getTime() {
        return time;
    }
}
