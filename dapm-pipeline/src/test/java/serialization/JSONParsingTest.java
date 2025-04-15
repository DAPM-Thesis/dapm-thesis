package serialization;

import communication.message.serialization.parsing.JSONParsing;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONParsingTest {

    public Object getJSONMap(String path) throws IOException {
        String contents = Files.readString(Paths.get(path));
        return JSONParsing.parse(contents);
    }

    @Test
    public void testJSONParsing() throws IOException {
        Object output = getJSONMap("src/test/resources/serialization/example.json");

        Map<String, Object> expected = new HashMap<>();
        expected.put("string", "text");
        expected.put("number", 42);
        expected.put("float", 4.2);
        expected.put("booleanTrue", true);
        expected.put("booleanFalse", false);
        expected.put("nullValue", null);
        expected.put("emptyObject", new HashMap<>());
        expected.put("emptyArray", new ArrayList<>());

        // array
        Map<String, Object> arrayObject = new HashMap<>();
        arrayObject.put("nested", "object");
        List<Object> array = new ArrayList<>(List.of(1, "two", true, arrayObject));
        array.add(null);
        expected.put("array", array);

        // doubly nested object
        List<Object> innerInnerArray = new ArrayList<>(List.of(false, 0, "end"));
        Map<String, Object> innerInnerObject = new HashMap<>();
        innerInnerObject.put("deep", innerInnerArray);
        Map<String, Object> innerObject = new HashMap<>();
        innerObject.put("nestedKey", innerInnerObject);
        expected.put("object", innerObject);

        assertEquals(output, expected);
    }
}
