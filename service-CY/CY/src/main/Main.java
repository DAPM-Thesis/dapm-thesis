package main;

import main.algorithms.BehavioralPatternsMiner;
import main.datatype.Event;
import main.datatype.petrinet.PetriNet;
import main.node.MiningNode;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collection;
import java.util.HashSet;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // an input topic should actually be coming from a Node. So this is a mock topic
        Topic<Event> inputTopicDiscovery = new Topic<>();
        Collection<Topic<?>> inputTopicsDiscovery = new HashSet<>();
        inputTopicsDiscovery.add(inputTopicDiscovery);
        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();

        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                inputTopicsDiscovery,
                discoveryMiner
        );

        System.out.println(discoveryNode.getOutputTopic().getClass());
        Message<Event> msg = new Message<>(new Event("Bob", "run", "15-13-25:19:50", new HashSet<>()));
        System.out.println(msg.data().getCaseID());
    }
}