package model;

public class Organization {

    private final int ID;
    private String name;

    public Organization(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public int getId() {
        return ID;
    }
}
