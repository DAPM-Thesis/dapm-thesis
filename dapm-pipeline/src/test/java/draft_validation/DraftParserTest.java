package draft_validation;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import draft_validation.parsing.DraftParser;
import draft_validation.parsing.InvalidDraft;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
    public void validDraft() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        System.out.println(path);

        // make source
        List<Class<? extends Message>> sourceInputs = new ArrayList<>();
        Class<? extends Message> sourceOutput = Event.class;
        MetadataProcessingElement source = new MetadataProcessingElement("Pepsi", "$$$ Source", sourceInputs, sourceOutput, 1);

        // make operator
        List<Class<? extends Message>> operatorInputs = List.of(Event.class);
        Class<? extends Message> operatorOutput = PetriNet.class;
        MetadataProcessingElement operator = new MetadataProcessingElement("Coca Cola", "The Profit Miner", operatorInputs, operatorOutput, 1);

        // make sink
        List<Class<? extends Message>> sinkInputs = List.of(PetriNet.class);
        Class<? extends Message> sinkOutput = null;
        MetadataProcessingElement sink = new MetadataProcessingElement("DTU", "Dream Sink", sinkInputs, sinkOutput, 1);

        Set<MetadataProcessingElement> expectedElements = Set.of(source, operator, sink);

        MetadataConsumer operatorPort1 = new MetadataConsumer(operator, 1);
        MetadataConsumer sinkPort1 = new MetadataConsumer(sink, 1);
        MetadataChannel c1 = new MetadataChannel(source, operatorPort1);
        MetadataChannel c2 = new MetadataChannel(operator, sinkPort1);
        Set<MetadataChannel> expectedChannels = Set.of(c1, c2);
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
    public void duplicateInvariance() {
        // It should not matter whether a channel or element exists twice [with same instanceID] in the given json
        String outputPath = "src/test/resources/draft_validation/parser/duplicate_invariance.json";
        PipelineDraft output = getPipelineDraft(outputPath);
        PipelineDraft expected = getSimpleValid();
        assertEquals(output, expected);
    }

    @Test
    public void empty() {
        String path = "src/test/resources/draft_validation/parser/empty.json";
        assertThrows(InvalidDraft.class, () -> {
            PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void singleElement() {
        String path = "src/test/resources/draft_validation/parser/single_element.json";
        assertThrows(InvalidDraft.class, () -> {
            PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        });
    }

    @Test
    public void noChannels() {
        String path = "src/test/resources/draft_validation/parser/no_channels.json";
        assertThrows(InvalidDraft.class, () -> {
            PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        });
    }

    // TODO: make error test for every assertion in DraftParser
}
