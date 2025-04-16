package serialization;

import communication.message.serialization.parsing.InvalidJXES;
import communication.message.serialization.parsing.JSONParser;
import communication.message.serialization.parsing.JXESParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JXESParserTest {
    // Since this class is just used for message deserialization, its positive cases are not tested here. Only the
    // negative cases are tested here. The positive cases are tested through the behavior in MessageDeserializerTest.

    public Object getJXESMap(String path) throws IOException {
        String contents = Files.readString(Paths.get(path));
        return (new JXESParser()).parse(contents);
    }

    @Test
    public void array() {
        // A JXES must start with an object, not an array
        assertThrows(InvalidJXES.class, () -> getJXESMap("" +
                "src/test/resources/serialization/JSONParser/empty_array_item.json"));
    }

    @Test
    public void emptyObject(){
        assertThrows(InvalidJXES.class, () -> getJXESMap(
                "src/test/resources/serialization/JXESParser/empty_object.json"
        ));
    }

    @Test
    public void tracesValueMissing() {
        assertThrows(InvalidJXES.class, () -> getJXESMap(
                "src/test/resources/serialization/JXESParser/traces_value_missing.json"
        ));
    }

    @Test
    public void emptyTraces() {
        assertThrows(InvalidJXES.class, () -> getJXESMap(
                "src/test/resources/serialization/JXESParser/empty_traces.json"));
    }
}
