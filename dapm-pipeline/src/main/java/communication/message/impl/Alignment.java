package communication.message.impl;

import communication.message.serialization.MessageVisitor;
import communication.message.Message;
import communication.message.serialization.deserialization.AlignmentDeserializationStrategy;
import communication.message.serialization.deserialization.DeserializationStrategy;

public class Alignment extends Message {
    private final Trace logTrace;
    private final Trace modelTrace;

    public Alignment(Trace logTrace, Trace modelTrace) {
        super(new AlignmentDeserializationStrategy());
        assert logTrace != null && modelTrace != null;
        assert !logTrace.isEmpty() && !modelTrace.isEmpty();
        assert logTrace.length() == modelTrace.length();

        this.logTrace = logTrace;
        this.modelTrace = modelTrace;
    }

    public Trace getLogTrace() { return logTrace; }
    public Trace getModelTrace() { return modelTrace; }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return new AlignmentDeserializationStrategy();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Alignment otherAlignment)) return false;
        return logTrace.equals(otherAlignment.logTrace) && modelTrace.equals(otherAlignment.modelTrace);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(logTrace, modelTrace);
    }
}
