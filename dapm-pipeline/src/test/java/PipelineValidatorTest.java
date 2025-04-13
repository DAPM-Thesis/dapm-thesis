import draft_validation.PipelineDraft;
import draft_validation.PipelineValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineValidatorTest {

    @Test
    public void SimpleValid() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft));
    }

    @Test
    public void NoSink() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void NoSource() {
        String path = "src/test/resources/draft_validation/no_sink.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void ChannelUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void ProcessingElementUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void ProducingSink() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/producing_sink.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void ConsumingSource() {
        // a sink which is the from element of a channel in the pipeline draft; a sink should always be the to-element
        String path = "src/test/resources/draft_validation/consuming_source.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void CyclicReflexive() {
        String path = "src/test/resources/draft_validation/cyclic_reflexive.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void CyclicIndirect() {
        String path = "src/test/resources/draft_validation/cyclic_indirect.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void SameTemplateConsumer() {
        // A pipeline should be able to contain two instances of the same template. This test suggests duplicates are handled correctly
        String path = "src/test/resources/draft_validation/same_template_consumer.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft));
    }


    @Test
    public void ChannelMismatchPortType() {
        // for any channel in the pipeline, the output type of the producer must match the input type for the given port of every consumer
        String path = "src/test/resources/draft_validation/channel_mismatch_port_type.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void ElementMissingProducers() {
        // every processing element in the elements of a pipeline must have all of their inputs produced to by channels
        String path = "src/test/resources/draft_validation/element_missing_producers.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    // TODO: make tests: channel inputs can mismatch outputs either when
        // 1) the port expected type does not match the produced type
        // 2) some element does not have producers for all of its inputs
        // 3) the same port of a consumer is produced to by more than 1 producer

    // TODO: make negative test for channel inputs match outputs (i.e. where they don't match)

    // TODO: test instanceID works correctly
        // TODO: make SameTypeProducer where two instances of same source template produce to the same sink instance
        // TODO: make negative test for both SameTemplateConsumer and SameTemplateProducer where the same instance is
             //  used as consumer producer. Try to make it such that the duplicate instance is not just removed as a
             // duplicate, e.g. by having some variation in channel; i.e. mismatching channels for the same instance [maybe isolated test?].

    // TODO: test that all elements are either a sink or a source or on a path from a sink to a source

}
