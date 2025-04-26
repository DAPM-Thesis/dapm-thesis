package pipeline.processingelement;

import utils.IDGenerator;

public abstract class ProcessingElement {
    private final int ID; // only used for illustrative toString purposes currently
    private boolean isAvailable = true;

    protected ProcessingElement() { ID = IDGenerator.generate(); }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getID() { return ID; }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public abstract void start();

    public abstract void stop();

    public abstract void terminate();

    @Override
    public String toString() { return getClass().getSimpleName() + ' ' + ID; }
}
