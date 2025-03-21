package datatype.serialization.deserialization;

import datatype.impl.Alignment;
import datatype.DataType;
import datatype.impl.Trace;
import datatype.impl.event.Event;
import datatype.impl.petrinet.PetriNet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DataTypeFactory {
    private static final HashMap<String, DeserializationStrategy> strategyMap = new HashMap<>();

    static {
        register(new Event("contents don't matter here; just give a constructor", "", "", new HashSet<>()));
        register(new PetriNet());
        register(new Alignment(new Trace(List.of(new Event("c", "a", "1", new HashSet<>()))),
                               new Trace(List.of(new Event("c", "a", "1", new HashSet<>())))));
        register(new Trace(new ArrayList<>()));
    }

    private static void register(DataType instance) {
        strategyMap.put(instance.getName(), instance.getDeserializationStrategy());
    }

    public static DataType deserialize(String serialization) {
        assert serialization != null && !serialization.isEmpty();

        String[] typeAndPayload = serialization.split(":", 2);
        assert typeAndPayload.length == 2 : "serialization pattern has changed from 'DataType_subtype:payload'";

        String className = typeAndPayload[0];
        DeserializationStrategy strategy = strategyMap.get(className);
        assert strategy != null : "deserialization for " + className + " has not been added to the DataTypeFactory";

        return strategy.deserialize(typeAndPayload[1]);
    }
}
