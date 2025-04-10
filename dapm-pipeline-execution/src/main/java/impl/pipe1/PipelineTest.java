package impl.pipe1;

import algorithm.Algorithm;
import communication.message.impl.event.Event;
import communication.message.Message;
import pipeline.PipelineBuilder;
import pipeline.processingelement.*;
import pipeline.processingelement.operator.SimpleOperator;

public class PipelineTest {

    public static void main(String[] args) {

        // Pipeline: Source<Event> -> Operator<Event, Event, String, String> -> Sink<Event>

        // Source
        Source<Event> source = new MyEventSource();

        // Event Operator
        Algorithm<Message, Event> algorithm = new MyEventAlgorithm();
        SimpleOperator<Event> operator = new MyEventOperator(algorithm);

        // Sink
        Sink sink = new MySink();

        ProcessingElementReference sourceReference = new ProcessingElementReference("org1", source.getID(), ProcessingElementType.SOURCE);
        ProcessingElementReference operatorReference = new ProcessingElementReference("org2", operator.getID(), ProcessingElementType.OPERATOR);
        ProcessingElementReference sinkReference = new ProcessingElementReference("org1", sink.getID(), ProcessingElementType.SINK);

//        // Create pipeline using pipeline builder
//        PipelineBuilder builder = new PipelineBuilder();
//                 builder.createPipeline("org1")
//                .addProcessingElement(sourceReference)
//                .addProcessingElement(operatorReference)
//                .addProcessingElement(sinkReference)
//                .connect(sourceReference, operatorReference)
//                .connect(operatorReference, sinkReference)
//                         .start();
    }
}
