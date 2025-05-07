package pipeline.processingelement;

import java.util.Map;

public abstract class ProcessingElement {
    protected final Configuration configuration;

    public ProcessingElement(Configuration configuration) { this.configuration = configuration; }
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public abstract boolean start();
    public abstract boolean pause();
    public abstract boolean terminate();

    @Override
    public String toString() { return getClass().getSimpleName() + ' ' + ID; }
}
