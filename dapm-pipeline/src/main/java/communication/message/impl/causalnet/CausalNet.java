package communication.message.impl.causalnet;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.impl.ProcessMap;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.CausalNetDeserializationStrategy;

import java.util.*;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = CausalNetDeserializationStrategy.class)
public class CausalNet extends Message {
    private final String label;
    private final Set<CausalNetNode> nodes = new HashSet<>();
    private final Map<CausalNetNode, Set<CausalNetBinding>> inputBindings = new HashMap<>();
    private final Map<CausalNetNode, Set<CausalNetBinding>> outputBindings = new HashMap<>();
    private CausalNetNode start = null;
    private CausalNetNode end = null;

    public CausalNet(String label) { this.label = label; }

    public String getLabel() { return label; }
    public Set<CausalNetNode> getNodes() { return nodes; }
    public Map<CausalNetNode, Set<CausalNetBinding>> getInputBindings() { return inputBindings; }
    public Map<CausalNetNode, Set<CausalNetBinding>> getOutputBindings() { return outputBindings; }
    public CausalNetNode getStart() { return start; }
    public CausalNetNode getEnd() { return end; }

    public Set<CausalNetBinding> getInputBindings(CausalNetNode node) {
        Set<CausalNetBinding> bindings = inputBindings.get(node);
        return (bindings == null) ? new HashSet<>() : bindings;
    }
    public Set<CausalNetBinding> getOutputBindings(CausalNetNode node) {
        Set<CausalNetBinding> bindings = outputBindings.get(node);
        return (bindings == null) ? new HashSet<>() : bindings;
    }
    public void setStartNode(CausalNetNode start) {
        assert (nodes.contains(start));
        this.start = start;
    }

    public void setEndNode(CausalNetNode end) { this.end = end; }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    public CausalNetNode addNode(CausalNetNode node) {
        nodes.add(node);
        if (!inputBindings.containsKey(node)) { inputBindings.put(node, new HashSet<>()); }
        if (!outputBindings.containsKey(node)) { outputBindings.put(node, new HashSet<>()); }
        return node;
    }

    public CausalNetBinding addInputBinding(CausalNetNode node, CausalNetNode... nodes) {
        CausalNetBinding binding = new CausalNetBinding(CausalNetBinding.Type.INPUT, node, nodes);
        inputBindings.get(node).add(binding);
        return binding;
    }
    
    public CausalNetBinding addOutputBinding(CausalNetNode node, CausalNetNode... nodes) {
        CausalNetBinding binding = new CausalNetBinding(CausalNetBinding.Type.OUTPUT, node, nodes);
        outputBindings.get(node).add(binding);
        return binding;
    }

    public CausalNetBinding addInputBinding(CausalNetNode node, Collection<? extends CausalNetNode> nodes) {
        CausalNetBinding binding = new CausalNetBinding(CausalNetBinding.Type.INPUT, node, nodes);
        inputBindings.get(node).add(binding);
        return binding;
    }

    public CausalNetBinding addOutputBinding(CausalNetNode node, Collection<? extends CausalNetNode> nodes) {
        CausalNetBinding binding = new CausalNetBinding(CausalNetBinding.Type.OUTPUT, node, nodes);
        outputBindings.get(node).add(binding);
        return binding;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CausalNet otherNet)) return false;
        return Objects.equals(label, otherNet.label)
                && Objects.equals(nodes, otherNet.nodes)
                && Objects.equals(inputBindings, otherNet.inputBindings)
                && Objects.equals(outputBindings, otherNet.outputBindings)
                && Objects.equals(start, otherNet.start)
                && Objects.equals(end, otherNet.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, nodes, inputBindings, outputBindings, start, end);
    }

}
