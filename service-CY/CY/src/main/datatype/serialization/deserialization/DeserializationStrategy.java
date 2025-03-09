package main.datatype.serialization.deserialization;

import main.datatype.DataType;

public interface DeserializationStrategy {
    DataType deserialize(String payload);
}
