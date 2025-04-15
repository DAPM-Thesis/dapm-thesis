package communication.message.impl;

import java.time.Instant;

import com.fasterxml.jackson.databind.DeserializationConfig;

import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategy;

public class Heartbeat extends Message {
    private final String senderId;
    private final Instant timestamp;
    private final TokenStatus tokenStatus;
    private final boolean immediateFlag;
    public Heartbeat(DeserializationStrategy strategy, String senderId, Instant timestamp, TokenStatus tokenStatus, boolean immediateFlag) {
        super(strategy);
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.tokenStatus = tokenStatus;
        this.immediateFlag = immediateFlag;
    }
    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }
    public String getSenderId() { return senderId; }
    public Instant getTimestamp() { return timestamp; }
    public TokenStatus getTokenStatus() { return tokenStatus; }
    public boolean isImmediateFlag() { return immediateFlag; }
    public enum TokenStatus { VALID, REVOKED }
}
