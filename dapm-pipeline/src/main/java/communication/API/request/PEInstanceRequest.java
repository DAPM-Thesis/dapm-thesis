package communication.API.request;

import communication.config.ConsumerConfig;

import java.util.List;
import java.util.Map;

public class PEInstanceRequest {

    private List<ConsumerConfig> consumerData;
    private Map<String, Object> configuration;

    public PEInstanceRequest() {
    }

    public List<ConsumerConfig> getConsumerConfigs() {
        return consumerData;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerData) {
        this.consumerData = consumerData;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
}
