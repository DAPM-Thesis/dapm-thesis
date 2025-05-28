package pipeline.processingelement;

import java.util.List;

public class HeartbeatConfiguration {
    private List<String> upstreamInstanceIds;
    private List<String> downstreamInstanceIds;

    public HeartbeatConfiguration() {}

    public void setUpstreamInstanceIds(List<String> upstreamInstanceIds) {
        this.upstreamInstanceIds = upstreamInstanceIds;
    }
    public List<String> getUpstreamInstanceIds() {
        return upstreamInstanceIds;
    }
    public void setDownstreamInstanceIds(List<String> downstreamInstanceIds) {
        this.downstreamInstanceIds = downstreamInstanceIds;
    }
    public List<String> getDownstreamInstanceIds() {
        return downstreamInstanceIds;
    }
}
