package datatype.event;

public class SingleAttribute<T> extends Attribute {
    private final String name;
    private T value;

    public SingleAttribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {return name;}
    public T getValue() {return value;}
}
