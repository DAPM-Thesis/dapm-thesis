package main.algorithms;

import main.datatype.DataType;
import main.datatype.petrinet.PetriNet;
import main.utils.Pair;

public class BehavioralPatternsMiner implements Algorithm<Pair<PetriNet, Boolean>> {
    AlgorithmConfiguration configuration;

    @Override
    public <U extends DataType> Pair<PetriNet, Boolean> runAlgorithm(U item) {
        return new Pair<>(new PetriNet(), false);
    }
}
