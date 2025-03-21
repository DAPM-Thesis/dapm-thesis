package impl;

import communication.channel.ChannelFactory;
import communication.channel.SimpleChannelFactory;
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
        ChannelFactory channelFactory = new SimpleChannelFactory();
        Pipeline pipeline = new Pipeline(channelFactory);
        pipeline.addProcessingElement(mySource);
        pipeline.addProcessingElement(operator);
        pipeline.addProcessingElement(sink);

        pipeline.connect(mySource, operator);
        pipeline.connect(operator, sink);

        pipeline.start();
    }
}
