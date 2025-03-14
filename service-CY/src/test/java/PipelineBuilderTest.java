import algorithms.BehavioralPatternsMiner;
import datatype.petrinet.PetriNet;
import model.Pipeline;
import model.Topic;
import node.MiningNode;
import node.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.PipelineBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineBuilderTest {

    @BeforeEach
    public void setUp() {
        PipelineBuilder.getInstance().reset();
    }

    @Test
    public void testPipelineBuilderInstance() {
        // Get pipeline instance
        PipelineBuilder builderInstance1 = PipelineBuilder.getInstance();
        PipelineBuilder builderInstance2 = PipelineBuilder.getInstance();

        // Assert
        assertSame(builderInstance1, builderInstance2);
    }

    @Test
    public void createPipeline() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create the pipeline
        int ID = 1;
        builder.createPipeline(ID);

        // Set up assert
        Pipeline pipeline = builder.getPipeline(ID);

        // Assert
        assertNotNull(pipeline);
        assertEquals(ID, pipeline.getID());
    }

    @Test
    public void createMultiplePipelines() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipelines
        int ID1 = 1;
        int ID2 = 2;
        int ID3 = 3;
        builder.createPipeline(ID1);
        builder.createPipeline(ID2);
        builder.createPipeline(ID3);

        // Set up assert
        Pipeline pipeline1 = builder.getPipeline(ID1);
        Pipeline pipeline2 = builder.getPipeline(ID2);
        Pipeline pipeline3 = builder.getPipeline(ID3);

        // Assert
        assertEquals(ID1, pipeline1.getID());
        assertEquals(ID2, pipeline2.getID());
        assertEquals(ID3, pipeline3.getID());
    }

    @Test
    public void createPipelinesWithSameID() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipelines with the same ID
        int ID1 = 1;
        builder.createPipeline(ID1);
        builder.createPipeline(ID1);

        // Set up assert
        HashMap<Integer, Pipeline> pipelines = builder.getPipelines();

        // Assert
        assertEquals(1, pipelines.size());
    }

    @Test
    public void addNodesToPipeline() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipeline
        int pipelineID = 1;
        builder.createPipeline(pipelineID);

        // Create nodes
        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        MiningNode<PetriNet> discoveryNode2 = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        // Connect nodes in pipeline
        builder.connectNodes(pipelineID, discoveryNode, discoveryNode2);

        // Set up assert
        int discoveryNodeID = discoveryNode.getID();
        int discoveryNode2ID = discoveryNode2.getID();

        Pipeline pipeline = builder.getPipeline(pipelineID);

        Node node1 = pipeline.getNodeByID(discoveryNodeID);
        Node node2 = pipeline.getNodeByID(discoveryNode2ID);

        // Assert
       assertEquals(discoveryNodeID, node1.getID());
       assertEquals(discoveryNode2ID, node2.getID());
    }

    @Test
    public void addNodesToDifferentPipelines() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipeline
        int pipelineID1 = 1;
        int pipelineID2 = 2;
        builder.createPipeline(pipelineID1);
        builder.createPipeline(pipelineID2);

        // Create nodes
        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        MiningNode<PetriNet> discoveryNode2 = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        // Connect nodes in pipeline
        builder.connectNodes(pipelineID1, discoveryNode, discoveryNode2);
        builder.connectNodes(pipelineID2, discoveryNode, discoveryNode2);

        // Set up assert
        int discoveryNode1ID = discoveryNode.getID();
        int discoveryNode2ID = discoveryNode2.getID();

        Pipeline pipeline1 = builder.getPipeline(pipelineID1);
        Pipeline pipeline2 = builder.getPipeline(pipelineID2);

        Node node1Pipeline1 = pipeline1.getNodeByID(discoveryNode1ID);
        Node node2Pipeline1 = pipeline1.getNodeByID(discoveryNode2ID);

        Node node1Pipeline2 = pipeline2.getNodeByID(discoveryNode1ID);
        Node node2Pipeline2 = pipeline2.getNodeByID(discoveryNode2ID);

        // Assert
        assertEquals(discoveryNode1ID, node1Pipeline1.getID());
        assertEquals(discoveryNode2ID, node2Pipeline1.getID());
        assertEquals(discoveryNode1ID, node1Pipeline2.getID());
        assertEquals(discoveryNode2ID, node2Pipeline2.getID());
    }

    @Test
    public void connectTwoNodesWithTopic() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipeline
        int pipelineID = 1;
        builder.createPipeline(pipelineID);

        // Create nodes
        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        MiningNode<PetriNet> discoveryNode2 = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        // Connect nodes
        builder.connectNodes(pipelineID, discoveryNode, discoveryNode2);

        // Set up assert
        Topic outputTopic = discoveryNode.getOutputTopic();
        Collection<Topic> inputTopics = discoveryNode2.getInputTopics();

        // Assert
        assertEquals(outputTopic, inputTopics.iterator().next());
        assertEquals(outputTopic.getName(), inputTopics.iterator().next().getName());
    }

    @Test
    public void miningNodeWithTwoInputTopics() {
        // Get pipeline instance
        PipelineBuilder builder = PipelineBuilder.getInstance();

        // Create pipeline
        int pipelineID = 1;
        builder.createPipeline(pipelineID);

        // Create nodes
        BehavioralPatternsMiner discoveryMiner = new BehavioralPatternsMiner();
        MiningNode<PetriNet> discoveryNode = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        MiningNode<PetriNet> discoveryNode2 = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        MiningNode<PetriNet> discoveryNode3 = new MiningNode<>(
                "DUMMY Discovery algorithm",
                "DUMMY discovery algorithm taking in events and outputting petri nets",
                discoveryMiner
        );

        // Connect nodes
        builder.connectNodes(pipelineID, discoveryNode, discoveryNode2);
        builder.connectNodes(pipelineID, discoveryNode3, discoveryNode2);

        // Set up assert
        Topic outputTopic = discoveryNode.getOutputTopic();
        Collection<Topic> inputTopics = discoveryNode2.getInputTopics();
        Iterator<Topic> iterator = inputTopics.iterator();
        Topic input1;
        Topic input2;

        // Assert
        assertEquals(2, inputTopics.size());
        if (iterator.hasNext()) {
            input1 = iterator.next();
            assertEquals(outputTopic.getName(), input1.getName());
        }

        if (iterator.hasNext()) {
            input2 = iterator.next();
            assertEquals(outputTopic.getName(), input2.getName());
        }
    }
}
