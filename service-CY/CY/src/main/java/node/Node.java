package main.node;


import main.utils.IDGenerator;

public abstract class Node {
    private final String name;
    private final int ID;
    private String description;

    public Node(String name, String description) {
        this.name = name;
        this.ID = IDGenerator.newID();
        this.description = description;
    }

    public String getName() { return name; }
    public int getID() { return ID; }
    public String getDescription() { return description; }

}
