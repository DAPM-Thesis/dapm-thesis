package main.datatype;

public class Attribute<T> {
    private final String name;
    private T value;

    public Attribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {return name;}
    public T getValue() {return value;}
}
