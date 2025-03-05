package main.datatype;

/** The type of items which can go over the stream. */
public abstract class DataType {

    public String stringRepresentation() {
        StringBuilder builder = new StringBuilder();
        return String.format("{concrete_type: %s, payload: %s}", getClass().toString(), getPayloadString());
    }

    protected abstract String getPayloadString();
}
