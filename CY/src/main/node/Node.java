package main.node;

public abstract class Node {
    private final String name;
    private final int ID;
    private String Description;

    public Node(String name, int ID, String Description) {
        this.name = name;
        this.ID = ID;
        this.Description = Description;
    }

    public String getName() { return name; }
    public int getID() { return ID; }
    public String getDescription() { return Description; }
}
