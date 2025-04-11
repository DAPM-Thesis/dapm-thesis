import draft_validation.MetadataChannel;
import draft_validation.MetadataProcessingElement;
import draft_validation.PipelineValidator;
import org.junit.jupiter.api.Test;
import utils.Pair;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineValidatorTest {

    @Test
    public void SimpleValid() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalNoSink() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalNoSource() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalChannelUnknownElement() {
        // The channels contains a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalProcessingElementUnknownElement() {
        // The channels contains a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalProducingSink() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/producing_sink.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void IllegalConsumingSource() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/consuming_source.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    public void IllegalCyclicReflexive() {
        String path = "src/test/resources/draft_validation/cyclic_reflexive.json";
        Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }
}
