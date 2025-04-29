package candidate_validation;

import candidate_validation.parsing.InvalidCandidate;

import java.util.Set;

public class ValidatedPipeline {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;

    /** Creates a ValidatedPipeline if the provided candidate meets certain requirements such as being acyclic and
     *  connected. Throws an error if the given pipeline is not valid. */
    public ValidatedPipeline(PipelineCandidate candidate) throws InvalidCandidate {
        if (!PipelineCandidateValidator.isValid(candidate)) {
            throw new InvalidCandidate("Candidate does not meet requirements for a pipeline, e.g. acyclicity, connectedness, etc.");
        }
        this.elements = candidate.getElements();
        this.channels = candidate.getChannels();
    }

    public Set<ProcessingElementReference> getElements() { return elements; }
    public Set<ChannelReference> getChannels() { return channels; }
}
