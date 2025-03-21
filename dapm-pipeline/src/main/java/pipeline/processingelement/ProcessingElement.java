package pipeline.processingelement;

public abstract class ProcessingElement {

    private boolean isAvailable = true;

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
