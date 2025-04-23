package communication.API;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import communication.config.ChannelConfig;

public class PEInstanceResponse {
    private final String templateID;
    private final String instanceID;
    private final ChannelConfig channelConfig;

    @JsonCreator
    public PEInstanceResponse(
            @JsonProperty("templateID") String templateID,
            @JsonProperty("instanceID") String instanceID,
            @JsonProperty("channelConfig") ChannelConfig channelConfig
    ) {
        this.templateID = templateID;
        this.instanceID = instanceID;
        this.channelConfig = channelConfig;
    }

    public static class Builder {
        private final String templateID;
        private final String instanceID;
        private ChannelConfig channelConfig;

        public Builder(String templateID, String instanceID) {
            this.templateID = templateID;
            this.instanceID = instanceID;
        }


        public Builder channelConfig(ChannelConfig config) {
            this.channelConfig = config;
            return this;
        }

        public PEInstanceResponse build() {
            return new PEInstanceResponse(templateID, instanceID, channelConfig);
        }
    }

    public String getTemplateID() {
        return templateID;
    }

    public ChannelConfig getChannelConfig() {return channelConfig;}

    public String getInstanceID() {
        return instanceID;
    }
}
