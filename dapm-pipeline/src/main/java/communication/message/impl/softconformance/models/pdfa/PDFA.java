package communication.message.impl.softconformance.models.pdfa;

import communication.message.serialization.parsing.JSONParser;

import java.util.*;

public class PDFA {
    private final Set<PDFANode> nodes = new LinkedHashSet<>();
    private Set<PDFAEdge> edges = new LinkedHashSet<>();

    private final Map<PDFANode, Collection<PDFAEdge>> inEdgeMap = new LinkedHashMap<>();
    private final Map<PDFANode, Collection<PDFAEdge>> outEdgeMap = new LinkedHashMap<>();
    private String attributeNameUsed;
    private double weightFactor;

    public Set<PDFANode> getNodes() { return nodes; }
    public Set<PDFAEdge> getEdges() { return edges; }
    public Map<PDFANode, Collection<PDFAEdge>> getInEdgeMap() { return inEdgeMap; }
    public Map<PDFANode, Collection<PDFAEdge>> getOutEdgeMap() { return outEdgeMap; }
    public String getAttributeNameUsed() { return attributeNameUsed; }

    public double getWeightFactor() { return weightFactor; }
    public void setWeightFactor(double weightFactor) { this.weightFactor = weightFactor; }
    public void setAttributeNameUsed(String attributeNameUsed) { this.attributeNameUsed = attributeNameUsed; }

    public synchronized boolean addNode(String label) {
        PDFANode snNode = new PDFANode(label);

        if (nodes.add(snNode)) {
            synchronized (inEdgeMap) { inEdgeMap.put(snNode, new LinkedHashSet<>()); }
            synchronized (outEdgeMap) { outEdgeMap.put(snNode, new LinkedHashSet<>()); }
            return true;
        }
        return false;
    }

    public synchronized boolean addEdge(String fromNodeLabel, String toNodeLabel, double probability) {
        PDFANode source = findNode(fromNodeLabel);
        PDFANode target = findNode(toNodeLabel);
        PDFAEdge trans = new PDFAEdge(source, target, probability);
        if (edges.add(trans)) {
            synchronized (inEdgeMap) { inEdgeMap.get(target).add(trans); }
            synchronized (outEdgeMap) { outEdgeMap.get(source).add(trans); }
            return true;
        }
        return false;
    }

    public synchronized PDFANode findNode(String identifier) {
        return nodes.stream()
                .filter(node -> node.label().equals(identifier))
                .findFirst()
                .orElse(null);
    }

    public synchronized PDFA getNewCopy() {
        PDFA pdfa = new PDFA();
        pdfa.attributeNameUsed = attributeNameUsed;
        pdfa.weightFactor = weightFactor;
        
        nodes.forEach(node -> pdfa.addNode(node.label()));
        edges.forEach(edge -> pdfa.addEdge(edge.getSource().label(), edge.getTarget().label(), edge.getProbability()));

        return pdfa;
    }

    public synchronized PDFAEdge findEdge(String fromNodeLabel, String toNodeLabel) {
        return edges.stream()
                .filter(edge -> edge.getSource().label().equals(fromNodeLabel)
                        && edge.getTarget().label().equals(toNodeLabel))
                .findFirst()
                .orElse(null);
    }

    public double getSequenceProbability(String source, String target) {
        PDFAEdge e = findEdge(source, target);
        return (e == null) ? 0d : e.getProbability();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PDFA otherPdfa)) return false;
        return nodes.equals(otherPdfa.nodes)
                && edges.equals(otherPdfa.edges)
                && inEdgeMap.equals(otherPdfa.inEdgeMap)
                && outEdgeMap.equals(otherPdfa.outEdgeMap)
                && (attributeNameUsed == null && otherPdfa.attributeNameUsed == null || attributeNameUsed.equals(otherPdfa.attributeNameUsed))
                && weightFactor == otherPdfa.weightFactor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges, inEdgeMap, outEdgeMap, attributeNameUsed, weightFactor);
    }
}
