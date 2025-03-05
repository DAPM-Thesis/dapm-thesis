package main.algorithms;

import main.datatype.DataType;
import main.utils.Pair;

/** Algorithm<T> where T is the output type of the runAlgorithm(), e.g. PetriNet or Event. Organization algorithms
 * must implement this interface. */
public interface Algorithm<T extends DataType> {

    /**
     * The algorithm which the organization algorithms must implement.
     *
     * @param item the stream item expected by the algorithm
     * @return A pair consisting of 1) the output of the algorithm, and 2) whether the mining node should publish the output
     */
    <U extends DataType> Pair<? extends DataType, Boolean> runAlgorithm(U item);
}
