package communication.message.impl.softconformance.models.pdfa;

import communication.message.impl.Alignment;

import java.util.Objects;

public class PDFAEdge {

    private double probability;
    private PDFANode source;
    private PDFANode target;

    public PDFAEdge(PDFANode source, PDFANode target, double probability) {
        this.source = source;
        this.target = target;
        this.probability = probability;
    }

    public PDFANode getSource() { return source; }
    public PDFANode getTarget() { return target; }
    public double getProbability() { return probability; }

    public void setProbability(double probability) { this.probability = probability; }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"from\":\"").append(source.serialize()).append("\",");
        sb.append("\"to\":\"").append(target.serialize()).append("\",");
        sb.append("\"probability\":").append(probability);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof PDFAEdge otherEdge)) return false;
        return source.equals(otherEdge.getSource()) && target.equals(otherEdge.getTarget());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return source + " -> " + target + " (" + probability + ")";
    }
}
