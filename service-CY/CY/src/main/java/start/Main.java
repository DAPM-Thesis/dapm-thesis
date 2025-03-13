package start;

import algorithms.BehavioralPatternsMiner;
import datatype.petrinet.PetriNet;
import model.Organization;
import model.Topic;
import node.MiningNode;
import node.Node;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.NodeRegistry;
import service.PipelineBuilder;

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

        Organization organization1 = new Organization(1, "O1");
        Organization organization2 = new Organization(2, "O2");

        // An organization registry could be useful

        // Organizations add nodes to registry
        NodeRegistry nodeRegistry = NodeRegistry.getInstance();
        nodeRegistry.addNode(organization1.getId(), discoveryNode);
        nodeRegistry.addNode(organization2.getId(), discoveryNode2);
        nodeRegistry.addNode(organization2.getId(), conformanceNode);

        // Retrieve nodes from registry to build pipeline
        discoveryNode = (MiningNode<PetriNet>) nodeRegistry.getNodeByID(organization1.getId(), discoveryNode.getID());
        discoveryNode2 = (MiningNode<PetriNet>) nodeRegistry.getNodeByID(organization2.getId(), discoveryNode2.getID());
        conformanceNode = (MiningNode<PetriNet>) nodeRegistry.getNodeByID(organization2.getId(), conformanceNode.getID());

        PipelineBuilder pipelineBuilder = PipelineBuilder.getInstance();
        pipelineBuilder.createPipeline(1);
        pipelineBuilder.connectNodes(1, discoveryNode, discoveryNode2)
                .connectNodes(1, discoveryNode, conformanceNode)
                .connectNodes(1, discoveryNode2, conformanceNode);

        for(Topic topic : conformanceNode.getInputTopics()) {
            System.out.println(topic.getName());
        }
    }
}