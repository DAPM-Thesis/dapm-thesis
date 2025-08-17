package candidate_validation;

import candidate_validation.parsing.CandidateParser;
import candidate_validation.parsing.JsonSchemaMismatch;
import candidate_validation.parsing.PipelineCandidateData;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.UserDefinedHeartbeatConfig;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import utils.Pair;

import java.net.URI;
import java.util.Objects;
import java.util.Set;


public class PipelineCandidate {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;
    private final FaultToleranceLevel faultToleranceLevel;
    private final UserDefinedHeartbeatConfig userDefinedHeartbeatConfig;

    /**
     * @param json              the JSON representation of a pipeline candidate
     * @param configFolderPath  the path to the configuration schema folder. this folder should contain a schema
     *                          for each processing element in 'json' which specifies the configurations and allowed
     *                          values for said processing elements.
     * @throws RuntimeException if parsing fails or json does not conform to the pipeline candidate and configuration
     *                          schemas.
     */
    public PipelineCandidate(String json, URI configFolderPath) throws JsonSchemaMismatch {
        PipelineCandidateData pipelineCandidateData = (new CandidateParser(configFolderPath)).deserialize(json);
        this.elements = pipelineCandidateData.elements();
        this.channels = pipelineCandidateData.channels();
        this.faultToleranceLevel = pipelineCandidateData.faultToleranceLevel();
        this.userDefinedHeartbeatConfig = pipelineCandidateData.userDefinedHeartbeatConfig();
    }

    public Set<ProcessingElementReference> getElements() { return Set.copyOf(elements); }
    public Set<ChannelReference> getChannels() { return Set.copyOf(channels); }
    public FaultToleranceLevel getFaultToleranceLevel() { return faultToleranceLevel; }
    public UserDefinedHeartbeatConfig getUserDefinedHeartbeatConfig() { return userDefinedHeartbeatConfig; }

    @Override
    public String toString() {
        return "PC[" +
                "elements=" + elements +
                ", channels=" + channels +
                ", faultToleranceLevel=" + faultToleranceLevel +
                ", userDefinedHeartbeatConfig=" + userDefinedHeartbeatConfig +
                ']';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PipelineCandidate otherPC)) return false;
        return Objects.equals(elements, otherPC.elements) &&
               Objects.equals(channels, otherPC.channels) &&
               faultToleranceLevel == otherPC.faultToleranceLevel &&
               Objects.equals(userDefinedHeartbeatConfig, otherPC.userDefinedHeartbeatConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, channels, faultToleranceLevel, userDefinedHeartbeatConfig);
    }
}
