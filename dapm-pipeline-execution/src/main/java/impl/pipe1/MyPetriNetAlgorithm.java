package impl.pipe1;

import algorithm.Algorithm;
import communication.message.Message;
import communication.message.impl.petrinet.PetriNet;

public class MyPetriNetAlgorithm implements Algorithm<Message, PetriNet> {
    @Override
    public PetriNet run(Message petriNet) {
        System.out.println(this.getClass().getSimpleName() + " applied.");
        return (PetriNet) petriNet;
    }
}
