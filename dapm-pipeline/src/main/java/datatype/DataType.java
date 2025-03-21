package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DeserializationStrategy;

public abstract class DataType {
    protected DeserializationStrategy deserializationStrategy;

    protected DataType(DeserializationStrategy strategy) { this.deserializationStrategy = strategy; }

    public DeserializationStrategy getDeserializationStrategy() { return deserializationStrategy; }

    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(DataTypeVisitor<?> v);
}
