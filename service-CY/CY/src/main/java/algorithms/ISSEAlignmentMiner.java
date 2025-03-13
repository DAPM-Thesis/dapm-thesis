package algorithms;

import datatype.DataMap;
import datatype.event.Event;
import utils.Pair;

/** An implementation of Schuster and van Zelst's paper "Online Process Monitoring Using Incremental State-Space
 * Expansion: An Exact Algorithm", and the implementation complementing the paper:
 * https://github.com/fit-daniel-schuster/online_process_monitoring_using_incremental_state-space_expansion_an_exact_algorithm/blob/master/pm4py/algo/conformance/alignments/incremental_a_star/incremental_a_star_approach.py
 * */
public class ISSEAlignmentMiner implements Algorithm<Event, Pair<DataMap, Boolean>> {
    @Override
    public Pair<DataMap, Boolean> runAlgorithm(Event item) {
        
        return null;
    }
}
