package main.datatype.petrinet;

public class Place {
    private String ID;
    private int marking;

    public Place(String ID, int marking) {
        assert marking >= 0 : "A place must have a non-negative number of tokens [marking]";
        this.ID = ID;
        this.marking = marking;
    }

    @Override
    public int hashCode() {return ID.hashCode();}

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Place otherPlace)) return false;
        return ID.equals(otherPlace.ID);
    }

}
