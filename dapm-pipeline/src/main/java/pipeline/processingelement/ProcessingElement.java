package pipeline.processingelement;

import utils.IDGenerator;

import java.util.Map;

public abstract class ProcessingElement {
    private final int ID; // only used for illustrative toString purposes currently
    private boolean isAvailable = true; // TODO: remove when R is done with ACPE
    private Map<String, Object> configuration;

    protected ProcessingElement() { ID = IDGenerator.generate(); }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getID() { return ID; }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

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
