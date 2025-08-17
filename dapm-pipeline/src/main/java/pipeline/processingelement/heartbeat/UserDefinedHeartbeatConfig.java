package pipeline.processingelement.heartbeat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDefinedHeartbeatConfig  {
    private final Long sendIntervalMs;
    private final Long checkIntervalMs;
    private final Long timeoutMs;
    private final Integer missesThreshold;

    @JsonCreator
    public UserDefinedHeartbeatConfig(
            @JsonProperty("sendIntervalMs") Long sendIntervalMs,
            @JsonProperty("checkIntervalMs") Long checkIntervalMs,
            @JsonProperty("timeoutMs") Long timeoutMs,
            @JsonProperty("missesThreshold") Integer missesThreshold) {
        this.sendIntervalMs = sendIntervalMs;
        this.checkIntervalMs = checkIntervalMs;
        this.timeoutMs = timeoutMs;
        this.missesThreshold = missesThreshold;
    }

    public Long getSendIntervalMs() { return sendIntervalMs; }
    public Long getCheckIntervalMs() { return checkIntervalMs; }
    public Long getTimeoutMs() { return timeoutMs; }
    public Integer getMissesThreshold() { return missesThreshold; }
}