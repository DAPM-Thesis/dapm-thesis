package main.datatype.petrinet;

public class Transition {
    private String ID;

    public Transition(String ID) { this.ID = ID; }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Transition otherTransition)) return false;
        return ID.equals(otherTransition.ID);
    }

    @Override
    public int hashCode() { return ID.hashCode(); }

}
