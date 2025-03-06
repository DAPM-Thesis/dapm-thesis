package main.datatype.petrinet.arc;

import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;

public class TransitionToPlaceArc extends Arc {
    private Transition source;
    private Place target;

    public TransitionToPlaceArc(String ID, Transition source, Place target) {
        super(ID);
        this.source = source;
        this.target = target;
    }

    public Transition getSource() { return source; }
    public Place getTarget() { return target; }
}
