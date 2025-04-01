package impl.pipe2;

import algorithm.Algorithm;
import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import impl.pipe1.*;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;
import pipeline.processingelement.operator.SimpleOperator;

public class PipelineTest2 {
    public static void main(String[] args) {
        /*
             petriNetSource -> petriNetOperator_
                                                \
                                                 --> sink
                                                /
            eventSource --------> eventOperator
         */

        // Sources
        Source<Event> eventSource = new MyEventSource();
        Source<PetriNet> petriNetSource = new MyPetriNetSource();

        // Event Operator; algorithm takes in string and returns same string
        Algorithm<Message, Event> eventAlgorithm = new MyEventAlgorithm();
        SimpleOperator<Event> eventOperator = new MyEventOperator(eventAlgorithm);
        // PetriNet Operator; algorithm takes in PetriNet and returns PetriNet
        Algorithm<Message, PetriNet> petriNetAlgorithm = new MyPetriNetAlgorithm();
        SimpleOperator<PetriNet> petriNetOperator = new MyPetriNetOperator(petriNetAlgorithm);


        // Sink
        Sink sink = new DualInputSink();

        // Create pipeline using pipeline builder
        PipelineBuilder builder = new PipelineBuilder();

        // petri net put in first
        Pipeline pipeline = builder.createPipeline()
            .addProcessingElement(petriNetSource)
            .addProcessingElement(petriNetOperator)
            .addProcessingElement(sink)
            .connect(petriNetSource, petriNetOperator)
            .connect(petriNetOperator, sink)

            .addProcessingElement(eventSource)
            .addProcessingElement(eventOperator)
            .connect(eventSource, eventOperator)
            .connect(eventOperator, sink)
            .getCurrentPipeline();

        pipeline.start();

        /*
        // Event first
        Pipeline pipeline = builder.createPipeline(channelFactory)
                .addProcessingElement(eventSource)
                .addProcessingElement(eventOperator)
                .addProcessingElement(sink)
                .connect(eventSource, eventOperator)
                .connect(eventOperator, sink)

                .addProcessingElement(petriNetSource)
                .addProcessingElement(petriNetOperator)
                .connect(petriNetSource, petriNetOperator)
                .connect(petriNetOperator, sink)

                .getCurrentPipeline();

        pipeline.start();

         */

    }

}
