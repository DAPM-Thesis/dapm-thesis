package impl;

import algorithm.Algorithm;
import message.impl.event.Event;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.processingelement.Operator;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;

public class PipelineTest {

    public static void main(String[] args) {

        // Pipeline: Source<Event> -> Operator<Event, Event, String, String> -> Sink<Event>

        // Source
        Source<Event> source = new MyEventSource();

        // Event Operator
        Algorithm<String, String> algorithm = new MyStringAlgorithm();
        Operator<Event, Event, String, String> operator = new MyEventOperator(algorithm);

        // Sink
        Sink<Event> sink = new MySink();

        // Create pipeline using pipeline builder
        PipelineBuilder builder = new PipelineBuilder();
        Pipeline pipeline = builder.createPipeline()
                .addProcessingElement(source)
                .addProcessingElement(operator)
                .addProcessingElement(sink)
                .connect(source, operator)
                .connect(operator, sink)
                .getCurrentPipeline();

        pipeline.start();
    }
}
