package candidate_validation;

import candidate_validation.parsing.CandidateParser;
import utils.Pair;

import java.util.Objects;
import java.util.Set;

public class PipelineCandidate {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;

    /** Creates a PipelineCandidate from a JSON string. Will throw an error if the JSON does not conform to the
     *  pipeline candidate JSON schema in resources/jsonschemas/pipeline_candidate_schema.json. */
    public PipelineCandidate(String json) throws RuntimeException {
        Pair<Set<ProcessingElementReference>, Set<ChannelReference>> elementsAndChannels
                = (new CandidateParser()).deserialize(json);
        this.elements = elementsAndChannels.first();
        this.channels = elementsAndChannels.second();
    }

    public Set<ProcessingElementReference> getElements() { return elements; }
    public Set<ChannelReference> getChannels() { return channels; }

    @Override
    public String toString() {
        return "PC[" +
                "elements=" + elements +
                ", channels=" + channels +
                ']';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PipelineCandidate otherPC)) return false;
        return elements.equals(otherPC.elements) && channels.equals(otherPC.channels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, channels);
    }
}
