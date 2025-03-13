package algorithms;

import datatype.Alignment;
import datatype.DataMap;
import datatype.Trace;
import datatype.event.Event;
import utils.Pair;

import java.util.HashSet;
import java.util.List;

/** An implementation of Schuster and van Zelst's paper "Online Process Monitoring Using Incremental State-Space
 * Expansion: An Exact Algorithm", and the implementation complementing the paper:
 * https://github.com/fit-daniel-schuster/online_process_monitoring_using_incremental_state-space_expansion_an_exact_algorithm/blob/master/pm4py/algo/conformance/alignments/incremental_a_star/incremental_a_star_approach.py
 * */
public class ISSEAlignmentMiner implements Algorithm<Event, Pair<Alignment, Boolean>> {
    @Override
    public Pair<Alignment, Boolean> runAlgorithm(Event item) {
        // TODO: should probably ultimately output a DataMap containing the data output by the article's implementation's output
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Event em1 = new Event("0", "A1", "0", new HashSet<>());
        Event em2 = new Event("0", "A3", "0", new HashSet<>());
        Trace logTrace = new Trace(List.of(el1, el2));
        Trace modelTrace = new Trace(List.of(em1, em2));
        Alignment alignment = new Alignment(logTrace, modelTrace);

        return new Pair<>(alignment, true);
    }
}
