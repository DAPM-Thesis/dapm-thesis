package impl;

import pipeline.Pipeline;

public class PipelineTest {

    public static void main(String[] args) {
        // Source
        MySource mySource = new MySource();

        // Operator
        MyAlgorithm algorithm = new MyAlgorithm();
        MyOperator operator = new MyOperator(algorithm);

        // Sink
        MySink sink = new MySink();

        // Create pipeline
        Pipeline pipeline = new Pipeline();
        pipeline.addProcessingElement(mySource);
        pipeline.addProcessingElement(operator);
        pipeline.addProcessingElement(sink);

        pipeline.connect(mySource, operator);
        pipeline.connect(operator, sink);

        pipeline.start();
    }
}
