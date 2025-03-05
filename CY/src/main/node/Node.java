package main.node;

import main.Topic;
import main.datatype.DataType;
import main.node.handle.InputHandle;
import main.utils.IDGenerator;

public abstract class Node {
    private final String name;
    private final int ID;
    private String Description;

    public Node(String name, String Description) {
        this.name = name;
        this.ID = IDGenerator.newID();
        this.Description = Description;
    }

    public String getName() { return name; }
    public int getID() { return ID; }
    public String getDescription() { return Description; }

}
