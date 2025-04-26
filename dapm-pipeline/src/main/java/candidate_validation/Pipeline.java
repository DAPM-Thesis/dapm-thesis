package candidate_validation;

import candidate_validation.parsing.InvalidCandidate;

import java.util.Set;

public class Pipeline {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;

    public Pipeline(PipelineCandidate candidate) throws InvalidCandidate {
        if (!PipelineCandidateValidator.isValid(candidate)) {
            throw new InvalidCandidate("Candidate does not meet requirements for a pipeline, e.g. acyclicity, connectedness, etc.");
        }
        this.elements = candidate.getElements();
        this.channels = candidate.getChannels();
    }
}
