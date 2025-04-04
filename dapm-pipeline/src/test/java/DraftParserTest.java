import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import draft_validation.MetadataChannel;
import draft_validation.MetadataProcessingElement;
import draft_validation.parsing.DraftParser;
import org.junit.jupiter.api.Test;
import utils.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DraftParserTest {

    public static Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> getPipelineDraft(String path) {
        String contents;
        try { contents = Files.readString(Paths.get(path)); }

        catch (IOException e) {
            System.out.println(System.getProperty("user.dir") + "\n\n");
            throw new RuntimeException(e);
        }

        return (new DraftParser()).deserialize(contents);
    }

    @Test
    public void ValidDraft() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        System.out.println(path);

        // make source
        List<Class<? extends Message>> sourceInputs = new ArrayList<>();
        List<Class<? extends Message>> sourceOutputs = List.of(Event.class);
        MetadataProcessingElement source = new MetadataProcessingElement("Pepsi", "$$$ Source", sourceInputs, sourceOutputs);

        // make operator
        List<Class<? extends Message>> operatorInputs = List.of(Event.class, PetriNet.class);
        List<Class<? extends Message>> operatorOutputs = List.of(PetriNet.class);
        MetadataProcessingElement operator = new MetadataProcessingElement("Coca Cola", "The Profit Miner", operatorInputs, operatorOutputs);

        // make sink
        List<Class<? extends Message>> sinkInputs = List.of(PetriNet.class);
        List<Class<? extends Message>> sinkOutputs = new ArrayList<>();
        MetadataProcessingElement sink = new MetadataProcessingElement("DTU", "Dream Sink", sinkInputs, sinkOutputs);

        List<MetadataProcessingElement> expectedElements = List.of(source, operator, sink);

        MetadataChannel c1 = new MetadataChannel(source, operator);
        MetadataChannel c2 = new MetadataChannel(operator, sink);
        List<MetadataChannel> expectedChannels = List.of(c1, c2);

        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> pipelineDraft = getPipelineDraft(path);
        Collection<MetadataProcessingElement> outputElements = pipelineDraft.getFirst();
        Collection<MetadataChannel> outputChannels = pipelineDraft.getSecond();
        System.out.println("LOL");

        // TODO: make sure this test fails; expected sets should currently be non-empty, and output is empty
        assertEquals(Set.of(expectedElements), Set.of(outputElements));
        assertEquals(Set.of(expectedChannels), Set.of(outputChannels));
    }

}
