package pipeline.processingelement.algorithm;

import utils.Pair;

public interface MiningAlgorithm<I, O> extends Algorithm<I, Pair<O, Boolean>> { }
