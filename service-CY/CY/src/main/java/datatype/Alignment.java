package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;

public class Alignment extends DataType {
    private final Trace logTrace;
    private final Trace modelTrace;

    public Alignment(Trace logTrace, Trace modelTrace) {
        this.logTrace = logTrace;
        this.modelTrace = modelTrace;
    }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return null;
    }
}
