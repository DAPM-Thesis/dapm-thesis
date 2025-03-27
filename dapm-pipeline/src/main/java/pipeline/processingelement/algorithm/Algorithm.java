package pipeline.processingelement.algorithm;

public interface Algorithm<I,O> {
    O run(I input);
}
