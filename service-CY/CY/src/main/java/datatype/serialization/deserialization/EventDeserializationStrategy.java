package datatype.serialization.deserialization;

import datatype.DataType;
import datatype.Event;

import java.util.HashSet;

public class EventDeserializationStrategy implements DeserializationStrategy {

    @Override
    public DataType deserialize(String payload) {
        // TODO: implement
        return new Event("not implemented", "not implemented", "not implemented", new HashSet<>());
    }
}
