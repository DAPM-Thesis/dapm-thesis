package main.algorithms;

import main.datatype.DataType;
import main.utils.Pair;

public interface Algorithm<T extends DataType> {

    public <U extends DataType> Pair<T, Boolean> runAlgorithm(U item);
}
