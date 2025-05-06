package pipeline.processingelement;

import java.util.Map;

public abstract class ProcessingElement {
    protected final Configuration configuration;

    public ProcessingElement(Configuration configuration) { this.configuration = configuration; }
}
