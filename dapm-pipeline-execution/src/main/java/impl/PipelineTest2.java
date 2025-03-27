package impl;

import algorithm.Algorithm;
import message.impl.event.Event;
import message.impl.petrinet.PetriNet;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import pipeline.processingelement.Operator;
import pipeline.processingelement.Sink;
import pipeline.processingelement.Source;

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
        Algorithm<String, String> eventAlgorithm = new MyStringAlgorithm();
        Operator<Event, Event, String, String> eventOperator = new MyEventOperator(eventAlgorithm);
        // PetriNet Operator; algorithm takes in PetriNet and returns PetriNet
        Algorithm<PetriNet, PetriNet> petriNetAlgorithm = new MyPetriNetAlgorithm();
        Operator<PetriNet, PetriNet, PetriNet, PetriNet> petriNetOperator = new MyPetriNetOperator(petriNetAlgorithm);


        // Sink
        /*
        Sink<EventPetriNetCompound> sink = new MySink();

        // Create pipeline using pipeline builder
        PipelineBuilder builder = new PipelineBuilder();
        ChannelFactory channelFactory = new SimpleChannelFactory();

        Pipeline pipeline = builder.createPipeline(channelFactory)
                .addProcessingElement(eventSource)
                .addProcessingElement(eventOperator)
                .addProcessingElement(sink)
                .connect(eventSource, eventOperator)
                .connect(eventOperator, sink)

                .addProcessingElement(petriNetOperator)
                .connect(petriNetSource, petriNetOperator)
                .connect(petriNetOperator, sink)

                .getCurrentPipeline();

        pipeline.start();

         */
    }

}
