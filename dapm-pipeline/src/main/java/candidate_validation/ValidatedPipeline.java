package candidate_validation;

import candidate_validation.parsing.InvalidCandidate;

import java.util.List;
import java.util.Set;

public class ValidatedPipeline {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;

    /** Creates a ValidatedPipeline if the provided candidate meets certain requirements such as being acyclic and
     *  connected. Throws an error if the given pipeline is not valid. */
    public ValidatedPipeline(PipelineCandidate candidate) throws InvalidCandidate {
        List<String> errors = PipelineCandidateValidator.validate(candidate);
        if (!errors.isEmpty()) {
            throw new InvalidCandidate("Candidate is invalid: \n" + String.join("\n", errors));
        }

        this.elements = candidate.getElements();
        this.channels = candidate.getChannels();
    }

    public Set<ProcessingElementReference> getElements() { return Set.copyOf(elements); }
    public Set<ChannelReference> getChannels() { return Set.copyOf(channels); }
}
