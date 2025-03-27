package impl.pipe1;

import algorithm.Algorithm;
import communication.message.impl.petrinet.PetriNet;
import pipeline.processingelement.operator.SimpleOperator;
import communication.message.Message;

import java.util.HashMap;
import java.util.Map;

public class MyPetriNetOperator extends SimpleOperator<PetriNet> {
    public MyPetriNetOperator(Algorithm<Message, PetriNet> petriNetAlgorithm) {
        super(petriNetAlgorithm);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(PetriNet.class, 1);
        return map;
    }
}
