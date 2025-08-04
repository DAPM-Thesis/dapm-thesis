package communication.message.impl.causalnet;

import communication.message.impl.ProcessMap;

public class CausalNetNode {
    private final String label;

    public CausalNetNode(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CausalNetNode otherNode)) return false;
        return label.equals(otherNode.label);
    }

    @Override
    public int hashCode() { return label.hashCode(); }
}
