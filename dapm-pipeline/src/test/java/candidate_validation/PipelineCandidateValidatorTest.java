package candidate_validation;

import candidate_validation.parsing.InvalidCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineCandidateValidatorTest {

    @Test
    public void simpleValid() {
        String path = "src/test/resources/candidate_validation/simple_valid.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertDoesNotThrow(() -> new ValidatedPipeline(candidate));
    }

    @Test
    public void channelUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/candidate_validation/channel_unknown_element.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void processingElementUnknownElement() {
        // The channels contain a channel with a processing element which is not in the processing elements list
        String path = "src/test/resources/candidate_validation/channel_unknown_element.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void cyclicReflexive() {
        String path = "src/test/resources/candidate_validation/cyclic_reflexive.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void cyclicIndirect() {
        String path = "src/test/resources/candidate_validation/cyclic_indirect.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void sameTemplateConsumer() {
        // A pipeline should be able to contain two instances of the same template. This test suggests duplicates are handled correctly
        String path = "src/test/resources/candidate_validation/same_template_consumer.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertDoesNotThrow(() -> new ValidatedPipeline(candidate));
    }


    @Test
    public void channelMismatchPortType() {
        // for any channel in the pipeline, the output type of the producer must match the input type for the given port of every consumer
        String path = "src/test/resources/candidate_validation/channel_mismatch_port_type.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void elementMissingProducers() {
        // every processing element in the elements of a pipeline must have all of their inputs produced to by channels
        String path = "src/test/resources/candidate_validation/element_missing_producers.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void samePortMultipleProducers() {
        String path = "src/test/resources/candidate_validation/same_port_multiple_producers.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void sameTemplateProducersToConsumer() {
        // Two instances of the same template produce to the same consumer. Validates instanceNumber works correctly
        String path = "src/test/resources/candidate_validation/same_template_producers_to_consumer.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertDoesNotThrow(() -> new ValidatedPipeline(candidate));
    }

    @Test
    public void pathEndsWithOperator() {
        // all complete pipeline paths must end with sinks
        String path = "src/test/resources/candidate_validation/path_ends_with_operator.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

    @Test
    public void pathStartsWithOperator() {
        String path = "src/test/resources/candidate_validation/path_starts_with_operator.json";
        PipelineCandidate candidate = CandidateParserTest.getPipelineCandidate(path);
        assertThrows(InvalidCandidate.class, () -> new ValidatedPipeline(candidate));
    }

}
