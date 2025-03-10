package algorithms;

import datatype.DataType;
import datatype.petrinet.PetriNet;
import utils.Pair;

public class BehavioralPatternsMiner implements Algorithm<Pair<PetriNet, Boolean>> {
    AlgorithmConfiguration configuration;

    @Override
    public <U extends DataType> Pair<PetriNet, Boolean> runAlgorithm(U item) {
        return new Pair<>(new PetriNet(), false);
    }
}
