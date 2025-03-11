package datatype.event;

import java.util.HashMap;
import java.util.Map;

public class NestedAttribute extends Attribute {
    private Map<String, Attribute> attributes;

    public NestedAttribute() {
        attributes = new HashMap<String, Attribute>();
    }

    public void put(String attributeName, Attribute attribute) {
        assert attributeName != null && attribute != null && attributes.containsKey(attributeName);
        attributes.put(attributeName, attribute);
    }
}
