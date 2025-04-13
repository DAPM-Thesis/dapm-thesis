import draft_validation.MetadataChannel;
import draft_validation.MetadataProcessingElement;
import draft_validation.PipelineValidator;
import org.junit.jupiter.api.Test;
import utils.Pair;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineValidatorTest {

    @Test
    public void SimpleValid() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void NoSink() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void NoSource() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void ChannelUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void ProcessingElementUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void ProducingSink() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/producing_sink.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void ConsumingSource() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/consuming_source.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void CyclicReflexive() {
        String path = "src/test/resources/draft_validation/cyclic_reflexive.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void CyclicIndirect() {
        String path = "src/test/resources/draft_validation/cyclic_indirect.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void SameTypeConsumer() {
        // A pipeline should be able to contain two instances of the same template. This test suggests duplicates are handled correctly
        String path = "src/test/resources/draft_validation/same_type_consumer.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    @Test
    public void ChannelInputsMatchOutputs() {
        String path = "src/test/resources/draft_validation/channel_inputs_match_outputs.json";
        Pair<Set<MetadataProcessingElement>, Set<MetadataChannel>> draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft.getFirst(), draft.getSecond()));
    }

    // TODO: test instanceID works correctly

    // TODO: test that all elements are either a sink or a source or on a path from a sink to a source

}
