package main.algorithms;

import main.datatype.DataType;
import main.datatype.PetriNet;
import main.utils.Pair;

public class BehavioralPatternsMiner implements Algorithm<PetriNet> {
    @Override
    public <U extends DataType> Pair<PetriNet, Boolean> runAlgorithm(U item) {
        return new Pair<>(new PetriNet(), false);
    }
}
