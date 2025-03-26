package impl;

import algorithm.Algorithm;
import message.impl.petrinet.PetriNet;
import pipeline.processingelement.Operator;

public class MyPetriNetOperator extends Operator<PetriNet, PetriNet, PetriNet, PetriNet> {
    public MyPetriNetOperator(Algorithm<PetriNet, PetriNet> petriNetAlgorithm) {
        super(petriNetAlgorithm);
    }

    @Override
    protected boolean publishCondition(PetriNet petriNet) {
        return true;
    }

    @Override
    protected PetriNet convertInput(PetriNet petriNet) {
        return petriNet;
    }

    @Override
    protected PetriNet convertOutput(PetriNet petriNet) {
        return petriNet;
    }
}
