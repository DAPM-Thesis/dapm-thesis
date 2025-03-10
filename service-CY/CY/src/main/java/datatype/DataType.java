package main.datatype;

import main.datatype.serialization.DataTypeVisitor;
import main.datatype.serialization.deserialization.DeserializationStrategy;

/** The type of items which can go over the stream. */
public abstract class DataType {

    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(DataTypeVisitor<?> v);

    public abstract DeserializationStrategy getDeserializationStrategy();
}
