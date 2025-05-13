package pipeline.processingelement;


public abstract class ProcessingElement {
    protected final Configuration configuration;

    public ProcessingElement(Configuration configuration) { this.configuration = configuration; }

    public abstract boolean start();
    public abstract boolean stop();
    public abstract boolean terminate();

}
