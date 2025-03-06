package main.datatype.petrinet;

import main.datatype.DataType;
import main.datatype.petrinet.arc.Arc;
import main.datatype.petrinet.arc.PlaceToTransitionArc;
import main.datatype.petrinet.arc.TransitionToPlaceArc;

import java.util.HashSet;
import java.util.Set;

public class PetriNet extends DataType {
    private Set<Place> places;
    private Set<Transition> transitions;
    private Set<Arc> flowRelation;

    public PetriNet() {
        places = new HashSet<>();
        transitions = new HashSet<>();
        flowRelation = new HashSet<>();
    }

    public PetriNet(Set<Place> places, Set<Transition> transitions, Set<Arc> flowRelation) {
        // add each component individually to assert invariants
        this();
        assert this.places.isEmpty() && this.transitions.isEmpty() && this.flowRelation.isEmpty() : "Empty constructor no longer compatible";

        for (Place p : places) {addPlace(p);}
        for (Transition t : transitions) {addTransition(t);}
        for (Arc a : flowRelation) {addArc(a);}
    }

    public void addPlace(Place p) {
        assert !places.contains(p) : "Place with the same id already exists";
        places.add(p);
    }
    public void addTransition(Transition t) {
        assert !transitions.contains(t) : "Transition with the same id already exists";
        transitions.add(t);
    }

    public void addArc(Arc a) {
        assert !flowRelation.contains(a) : "Arc with the same id already exists";
        if (a instanceof PlaceToTransitionArc pta) {
            assert places.contains(pta.getSource()) && transitions.contains(pta.getTarget()) : "An arc must be between an existing place and transition";
        } else if (a instanceof TransitionToPlaceArc tpa) {
            assert transitions.contains(tpa.getSource()) : "An arc must be between an existing transition and place";;
        } else {
            throw new IllegalCallerException("The given arc is not supported");
        }
        flowRelation.add(a);
    }

}
