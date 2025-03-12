package start;

import algorithms.BehavioralPatternsMiner;
import datatype.Event;
import datatype.petrinet.PetriNet;
import model.Message;
import model.Pipeline;
import model.Topic;
import node.MiningNode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.PipeLineBuilder;

import java.util.Collection;
import java.util.HashSet;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
 @SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // an input topic should actually be coming from a Node. So this is a mock topic

        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );
        discoveryNode.setInputTopic(new Topic("ingest"));

        BehavioralPatternsMiner discoveryMiner2 = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode2 = new MiningNode<>(
                "DUMMY Discovery algorithm2",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner2
        );

        // Pretend it is a conformance node
        BehavioralPatternsMiner conformanceAlgorithm = new BehavioralPatternsMiner();
        MiningNode<PetriNet> conformanceNode = new MiningNode<>(
                "DUMMY CONFORMANCE",
                "TESTING CONFORMANCE INPUT HANDLES",
                conformanceAlgorithm
        );

        PipeLineBuilder pipeLineBuilder = new PipeLineBuilder();
        pipeLineBuilder.connectNodes(discoveryNode, conformanceNode);
        pipeLineBuilder.connectNodes(discoveryNode2, conformanceNode);
        pipeLineBuilder.run();

        for(Topic topic : conformanceNode.getInputTopics()) {
            System.out.println(topic.getName());
        }
    }
}