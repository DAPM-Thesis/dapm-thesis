package candidate_validation;

import candidate_validation.parsing.InvalidCandidate;
import candidate_validation.parsing.JsonSchemaMismatch;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.UserDefinedHeartbeatConfig;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ValidatedPipeline {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;
    private final FaultToleranceLevel faultToleranceLevel;
    private final UserDefinedHeartbeatConfig userDefinedHeartbeatConfig;

    /** Creates a ValidatedPipeline if the provided candidate meets certain requirements such as being acyclic and
     *  connected. Throws an error if the given pipeline is not valid. */
    public ValidatedPipeline(PipelineCandidate candidate) throws InvalidCandidate {
        List<String> errors = PipelineCandidateValidator.validate(candidate);
        if (!errors.isEmpty()) {
            throw new InvalidCandidate("Candidate is invalid: \n" + String.join("\n", errors));
        }

        this.elements = candidate.getElements();
        this.channels = candidate.getChannels();
        this.faultToleranceLevel = candidate.getFaultToleranceLevel();
        this.userDefinedHeartbeatConfig = candidate.getUserDefinedHeartbeatConfig();
    }

    public ValidatedPipeline(String json, URI configFolderPath) throws JsonSchemaMismatch, InvalidCandidate {
        this(new PipelineCandidate(json, configFolderPath));
    }

    public Set<ProcessingElementReference> getElements() { return Set.copyOf(elements); }
    public Set<ChannelReference> getChannels() { return Set.copyOf(channels); }
    public FaultToleranceLevel getFaultToleranceLevel() { return faultToleranceLevel; }
    public UserDefinedHeartbeatConfig getUserDefinedHeartbeatConfig() { return userDefinedHeartbeatConfig; }

    // @Override
    // public boolean equals(Object other) {
    //     if (this == other) return true;
    //     if (!(other instanceof ValidatedPipeline otherPC)) return false;
    //     return elements.equals(otherPC.elements) && channels.equals(otherPC.channels);
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidatedPipeline that = (ValidatedPipeline) o;
        return Objects.equals(elements, that.elements) &&
               Objects.equals(channels, that.channels) &&
               faultToleranceLevel == that.faultToleranceLevel &&
               Objects.equals(userDefinedHeartbeatConfig, that.userDefinedHeartbeatConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, channels, faultToleranceLevel, userDefinedHeartbeatConfig);
    }
}
