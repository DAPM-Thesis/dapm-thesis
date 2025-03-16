package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;

public class Alignment extends DataType {
    private final Trace logTrace;
    private final Trace modelTrace;

    public Alignment(Trace logTrace, Trace modelTrace) {
        assert logTrace != null && modelTrace != null;
        assert logTrace.length() > 0 && modelTrace.length() > 0;
        assert logTrace.length() == modelTrace.length();

        this.logTrace = logTrace;
        this.modelTrace = modelTrace;
    }

    public Trace getLogTrace() { return logTrace; }
    public Trace getModelTrace() { return modelTrace; }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return null;
    }
}
