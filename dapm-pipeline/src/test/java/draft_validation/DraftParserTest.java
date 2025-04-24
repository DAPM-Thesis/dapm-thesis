package draft_validation;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import draft_validation.parsing.DraftParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DraftParserTest {

    public static PipelineDraft getPipelineDraft(String path) {
        String contents;
        try { contents = Files.readString(Paths.get(path)); }
        catch (IOException e) {
            System.out.println(System.getProperty("user.dir") + "\n\n");
            throw new RuntimeException(e);
        }

        return (new DraftParser()).deserialize(contents);
    }

    public static PipelineDraft getSimpleValid() {
        String simpleValidPath = "src/test/resources/draft_validation/simple_valid.json";
        return getPipelineDraft(simpleValidPath);
    }

    @Test
    public void schemaLoadable() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jsonschemas/pipeline_draft_schema.json");
        assertNotNull(is); // Should NOT be null
    }

    @Test
    public void validDraft() {
        String path = "src/test/resources/draft_validation/simple_valid.json";

        // make source
        List<Class<? extends Message>> sourceInputs = null;
        Class<? extends Message> sourceOutput = Event.class;
        ProcessingElementReference source = new ProcessingElementReference("Pepsi", "http://localhost:8082", "$$$ Source", sourceInputs, sourceOutput, 1);

        // make operator
        List<Class<? extends Message>> operatorInputs = List.of(Event.class);
        Class<? extends Message> operatorOutput = PetriNet.class;
        ProcessingElementReference operator = new ProcessingElementReference("Coca Cola", "http://localhost:8092","The Profit Miner", operatorInputs, operatorOutput, 1);

        // make sink
        List<Class<? extends Message>> sinkInputs = List.of(PetriNet.class);
        Class<? extends Message> sinkOutput = null;
        ProcessingElementReference sink = new ProcessingElementReference("DTU", "http://localhost:8102", "Dream Sink", sinkInputs, sinkOutput, 1);

        Set<ProcessingElementReference> expectedElements = Set.of(source, operator, sink);

        SubscriberReference operatorPort1 = new SubscriberReference(operator, 1);
        SubscriberReference sinkPort1 = new SubscriberReference(sink, 1);
        ChannelReference c1 = new ChannelReference(source, operatorPort1);
        ChannelReference c2 = new ChannelReference(operator, sinkPort1);
        Set<ChannelReference> expectedChannels = Set.of(c1, c2);
        PipelineDraft expected = new  PipelineDraft(expectedElements, expectedChannels);

        PipelineDraft output = getPipelineDraft(path);

        assertEquals(expected, output);
    }

    @Test
    public void elementOrderInvariance() {
        String outputPath = "src/test/resources/draft_validation/parser/element_order_invariance.json";
        PipelineDraft output = getPipelineDraft(outputPath);
        PipelineDraft expected = getSimpleValid();
        assertEquals(output, expected);
    }

    @Test
    public void channelOrderInvariance() {
        String outputPath = "src/test/resources/draft_validation/parser/channel_order_invariance.json";
        PipelineDraft output = getPipelineDraft(outputPath);
        PipelineDraft expected = getSimpleValid();
        assertEquals(output, expected);
    }

    @Test
    public void duplicate() {
        // TODO: update comment and file name; it should not be "invariant"
        // It should not matter whether a channel or element exists twice [with same instanceID] in the given json
        String outputPath = "src/test/resources/draft_validation/parser/duplicate.json";
        assertThrows(RuntimeException.class, () -> getPipelineDraft(outputPath));
    }

    @Test
    public void empty() {
        String path = "src/test/resources/draft_validation/parser/empty.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void singleElement() {
        String path = "src/test/resources/draft_validation/parser/single_element.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void noChannels() {
        String path = "src/test/resources/draft_validation/parser/no_channels.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void emptyArraySource() {
        // A source must be represented by null (by convention) - not by an empty array.
        String path = "src/test/resources/draft_validation/parser/empty_array_source.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void parameterValueParsing() {
        String path = "src/test/resources/draft_validation/parser/parameter_value_parsing.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        ProcessingElementReference source = draft.elements().stream()
                .filter(e -> e.getTemplateID().equals("$$$ Source"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("parsing of $$$ Source parameter Values failed."));

        List<Object> output = source.getParameterValues();
        List<Object> expected = List.of(0.5, 0.5, true, "string");
        assertEquals(expected, output);
    }

    @Test
    public void noSource() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void noSink() {
        String path = "src/test/resources/draft_validation/parser/no_sink.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void producingSink() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/parser/producing_sink.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void consumingSource() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/parser/consuming_source.json";
        assertThrows(RuntimeException.class, () -> {
            DraftParserTest.getPipelineDraft(path);
        });
    }
}
