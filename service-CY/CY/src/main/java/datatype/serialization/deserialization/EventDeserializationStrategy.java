package main.datatype.serialization.deserialization;

import main.datatype.DataType;
import main.datatype.Event;

import java.util.HashSet;

public class EventDeserializationStrategy implements DeserializationStrategy {

    @Override
    public DataType deserialize(String payload) {
        // TODO: implement
        return new Event("not implemented", "not implemented", "not implemented", new HashSet<>());
    }
}
