package main.datatype.petrinet.arc;

import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;

public class PlaceToTransitionArc extends Arc {
    private Place source;
    private Transition target;

    public PlaceToTransitionArc(String ID, Place source, Transition target) {
        super(ID);
        this.source = source;
        this.target = target;
    }

    public Place getSource() { return source; }

    public Transition getTarget() { return target; }
}
