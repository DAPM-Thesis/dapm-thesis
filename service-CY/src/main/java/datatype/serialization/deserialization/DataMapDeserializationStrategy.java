package datatype.serialization.deserialization;

import datatype.DataMap;
import datatype.DataType;

import java.util.HashMap;
import java.util.Map;

public class DataMapDeserializationStrategy implements DeserializationStrategy {
    @Override
    public DataType deserialize(String payload) {

        Map<String, Object> conformanceOutput = new HashMap<>();
        conformanceOutput.put("conformance", 0.753);
        conformanceOutput.put("completeness", 0.324503);
        conformanceOutput.put("confidence", 1.0);
        return new DataMap(conformanceOutput);
    }
}
