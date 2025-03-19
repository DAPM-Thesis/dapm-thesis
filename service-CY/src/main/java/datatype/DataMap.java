package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DataMapDeserializationStrategy;
import datatype.serialization.deserialization.DeserializationStrategy;

import java.util.HashMap;
import java.util.Map;

public class DataMap extends DataType {
    private final Map<String, Object> keyValuePairs;


    public DataMap() {
        keyValuePairs = new HashMap<>();
    }
    public DataMap(Map<String, Object> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    public Map<String, Object> getKeyValuePairs() { return keyValuePairs; }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) { v.visit(this); }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return new DataMapDeserializationStrategy();
    }

    public void put(String key, Object value) { keyValuePairs.put(key, value); }

    public boolean isEmpty() { return keyValuePairs.isEmpty(); }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DataMap otherDataMap)) return false;
        return keyValuePairs.equals(otherDataMap.keyValuePairs);
    }

    @Override
    public int hashCode() { return keyValuePairs.hashCode(); }
}
