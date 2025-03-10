package main.datatype.serialization.deserialization;

import main.datatype.DataType;
import main.datatype.Event;
import main.datatype.petrinet.PetriNet;

import java.util.HashMap;
import java.util.HashSet;

public class DataTypeFactory {
    private static final HashMap<String, DeserializationStrategy> strategyMap = new HashMap<>();

    static {
        register(new PetriNet());
        register(new Event("contents don't matter here; just give a constructor", "", "", new HashSet<>()));
    }

    private static void register(DataType instance) {
        strategyMap.put(instance.getName(), instance.getDeserializationStrategy());
    }

    public DataType deserialize(String serialization) {
        assert serialization != null && !serialization.isEmpty();

        String[] typeAndPayload = serialization.split(":", 2);
        assert typeAndPayload.length == 2 : "serialization pattern has changed from 'DataType_subtype:payload'";

        String className = typeAndPayload[0];
        DeserializationStrategy strategy = strategyMap.get(className);
        assert strategy != null : "deserialization for " + className + " has not been added to the DataTypeFactory";

        return strategy.deserialize(typeAndPayload[1]);
    }
}
