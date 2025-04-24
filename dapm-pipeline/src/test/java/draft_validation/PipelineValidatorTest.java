package draft_validation;

import draft_validation.parsing.InvalidDraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineValidatorTest {

    @Test
    public void simpleValid() {
        String path = "src/test/resources/draft_validation/simple_valid.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft));
    }

    @Test
    public void channelUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void processingElementUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/draft_validation/channel_unknown_element.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void cyclicReflexive() {
        String path = "src/test/resources/draft_validation/cyclic_reflexive.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void cyclicIndirect() {
        String path = "src/test/resources/draft_validation/cyclic_indirect.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void sameTemplateConsumer() {
        // A pipeline should be able to contain two instances of the same template. This test suggests duplicates are handled correctly
        String path = "src/test/resources/draft_validation/same_template_consumer.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft));
    }


    @Test
    public void channelMismatchPortType() {
        // for any channel in the pipeline, the output type of the producer must match the input type for the given port of every consumer
        String path = "src/test/resources/draft_validation/channel_mismatch_port_type.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void elementMissingProducers() {
        // every processing element in the elements of a pipeline must have all of their inputs produced to by channels
        String path = "src/test/resources/draft_validation/element_missing_producers.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void samePortMultipleProducers() {
        String path = "src/test/resources/draft_validation/same_port_multiple_producers.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

    @Test
    public void sameTemplateProducersToConsumer() {
        // Two instances of the same template produce to the same consumer. Validates instanceNumber works correctly
        String path = "src/test/resources/draft_validation/same_template_producers_to_consumer.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertTrue(PipelineValidator.isValid(draft));
    }

    @Test
    public void pathEndsWithOperator() {
        // all complete pipeline paths must end with sinks
        String path = "src/test/resources/draft_validation/path_ends_with_operator.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertThrows(InvalidDraft.class, () -> PipelineValidator.isValid(draft));
    }

    @Test
    public void pathStartsWithOperator() {
        String path = "src/test/resources/draft_validation/path_starts_with_operator.json";
        PipelineDraft draft = DraftParserTest.getPipelineDraft(path);
        assertFalse(PipelineValidator.isValid(draft));
    }

}
