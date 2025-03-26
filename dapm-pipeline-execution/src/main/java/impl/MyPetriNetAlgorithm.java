package impl;

import algorithm.Algorithm;
import message.impl.petrinet.PetriNet;

public class MyPetriNetAlgorithm implements Algorithm<PetriNet, PetriNet> {
    @Override
    public PetriNet run(PetriNet petriNet) {
        return petriNet;
    }
}
