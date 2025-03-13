package algorithms;

import datatype.DataType;
import utils.Pair;

/** Algorithm<T> where T is the output type of the runAlgorithm(), e.g. PetriNet or Event. Organization algorithms
 * must implement this interface. */
public interface Algorithm<I,O> {

    /**
     * The algorithm which the organization algorithms must implement.
     *
     * @param item the stream item expected by the algorithm
     * @return A pair consisting of 1) the output of the algorithm, and 2) whether the mining node should publish the output
     */
    O runAlgorithm(I item);
}
