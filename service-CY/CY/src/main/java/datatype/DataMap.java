package datatype;

import datatype.serialization.DataTypeVisitor;
import datatype.serialization.deserialization.DataMapDeserializationStrategy;
import datatype.serialization.deserialization.DeserializationStrategy;

import java.util.HashMap;
import java.util.Map;

public class DataMap extends DataType {
    private final Map<String, Object> keyValuePairs;


    public DataMap() {
        keyValuePairs = new HashMap<String, Object>();
    }
    public DataMap(Map<String, Object> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    @Override
    public void acceptVisitor(DataTypeVisitor<?> v) { v.visit(this); }

    @Override
    public DeserializationStrategy getDeserializationStrategy() {
        return new DataMapDeserializationStrategy();
    }

    public void put(String key, Object value) { keyValuePairs.put(key, value); }

}
