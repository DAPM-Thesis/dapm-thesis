package pipeline.processingelement;

import java.util.Map;

public class Configuration {
    private final Map<String, Object> configuration;

    public Configuration(Map<String, Object> configuration) { this.configuration = configuration; }

    public Object get(String key) { return configuration.get(key); }

}
