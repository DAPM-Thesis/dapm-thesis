package candidate_validation.parsing;

import java.util.Set;

import candidate_validation.ChannelReference;
import candidate_validation.ProcessingElementReference;
import pipeline.processingelement.heartbeat.FaultToleranceLevel;
import pipeline.processingelement.heartbeat.UserDefinedHeartbeatConfig;

public record PipelineCandidateData(
    Set<ProcessingElementReference> elements,
    Set<ChannelReference> channels,
    FaultToleranceLevel faultToleranceLevel,
    UserDefinedHeartbeatConfig userDefinedHeartbeatConfig
) {}