package communication.message.impl.causalnet;

import communication.message.impl.ProcessMap;

import java.util.*;

public class CausalNetBinding {
    public enum Type {
        INPUT,
        OUTPUT,
    }

    private final Type type;
    private final CausalNetNode node;
    private final Set<? extends CausalNetNode> boundNodes;

    public CausalNetBinding(Type type, CausalNetNode node, CausalNetNode... nodes) { this(type, node, Arrays.asList(nodes)); }

    public CausalNetBinding(Type type, CausalNetNode node, Collection<? extends CausalNetNode> boundNodes) {
        this.type = type;
        this.node = node;
        this.boundNodes = new HashSet<CausalNetNode>(boundNodes);
    }

    public String getType() { return type.toString(); }
    public CausalNetNode getNode() { return node; }
    public Set<CausalNetNode> getBoundNodes() { return new HashSet<>(boundNodes); }

    @Override
    public String toString() {
        return "CNB[" + type + ", " + node.toString() + ", " + boundNodes.toString() + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CausalNetBinding otherBinding)) return false;
        return type.equals(otherBinding.type)
                && node.equals(otherBinding.node)
                && boundNodes.equals(otherBinding.boundNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, node, boundNodes);
    }

}
