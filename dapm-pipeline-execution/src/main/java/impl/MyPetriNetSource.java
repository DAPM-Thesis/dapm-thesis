package impl;

import datatype.impl.petrinet.PetriNet;
import pipeline.processingelement.Source;

public class MyPetriNetSource extends Source<PetriNet> {
    @Override
    public PetriNet process() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new PetriNet();
    }
}
