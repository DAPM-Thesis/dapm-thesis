package impl.pipe1;

import communication.message.impl.petrinet.PetriNet;
import pipeline.processingelement.Source;

public class MyPetriNetSource extends Source<PetriNet> {
    @Override
    public PetriNet process() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new PetriNet();
    }
}
