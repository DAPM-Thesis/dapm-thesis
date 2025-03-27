package pipeline.processingelement;

import utils.IDGenerator;

public abstract class ProcessingElement {
    private int ID; // only used for illustrative toString purposes currently
    private boolean isAvailable = true;

    protected ProcessingElement() { ID = IDGenerator.generate(); }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ' ' + ID; }
}
