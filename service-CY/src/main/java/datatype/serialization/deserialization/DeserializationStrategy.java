package datatype.serialization.deserialization;

import datatype.DataType;

public interface DeserializationStrategy {
    DataType deserialize(String payload);
}
