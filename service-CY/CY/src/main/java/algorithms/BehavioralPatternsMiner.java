package algorithms;

import datatype.DataMap;
import datatype.DataType;
import datatype.petrinet.PetriNet;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class BehavioralPatternsMiner implements Algorithm<DataType, Pair<DataMap, Boolean>> {
    AlgorithmConfiguration configuration;

    @Override
    public Pair<DataMap, Boolean> runAlgorithm(DataType item) {
        Map<String, Object> conformanceOutput = new HashMap<>();
        conformanceOutput.put("conformance", 0.753);
        conformanceOutput.put("completeness", 0.324503);
        conformanceOutput.put("confidence", 1.0);

        return new Pair<>(new DataMap(conformanceOutput), true);
    }
}
