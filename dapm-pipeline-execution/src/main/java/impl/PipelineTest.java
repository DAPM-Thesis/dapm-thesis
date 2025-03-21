package impl;

import communication.channel.ChannelFactory;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;

public class PipelineTest {

    public static void main(String[] args) {
        // Source
        MySource mySource = new MySource();

        // Operator
        MyAlgorithm algorithm = new MyAlgorithm();
        MyOperator operator = new MyOperator(algorithm);

        // Sink
        MySink sink = new MySink();

        StringBuilder sb = new StringBuilder();

        // Create pipeline using pipeline builder
        PipelineBuilder builder = new PipelineBuilder();
        ChannelFactory channelFactory = new SimpleChannelFactory();

        builder.createPipeline(channelFactory)
                .addProcessingElement(mySource)
                .addProcessingElement(operator)
                .addProcessingElement(sink)
                .connect(mySource, operator)
                .connect(operator, sink)
                .getCurrentPipeline()
                .start();
    }
}
