package communication.API.request;

import communication.config.ConsumerConfig;

import java.util.List;
import java.util.Map;

public class PEInstanceRequest {

    private List<ConsumerConfig> consumerConfigs;
    private Map<String, Object> configuration;

    public PEInstanceRequest() {
    }

    public List<ConsumerConfig> getConsumerConfigs() {
        return consumerConfigs;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}
