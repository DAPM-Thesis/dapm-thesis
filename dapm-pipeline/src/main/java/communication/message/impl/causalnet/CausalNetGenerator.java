package communication.message.impl.causalnet;

import utils.Pair;

import java.util.*;


/**
 *
 * @author Andrea Burattin
 */
public class CausalNetGenerator {
    public final String ARTIFICIAL_START_NAME = "ARTIFICIAL_START";
    public final String ARTIFICIAL_END_NAME = "ARTIFICIAL_END";

    private HashMap<String, Double> activities;
    private HashMap<Pair<String, String>, Double> relations;

    private HashMap<String, Double> activityFrequency;
    private Set<String> startEvents;
    private Set<String> endEvents;

    private Map<String, Integer> startingActivities;
    private Map<String, Integer> finishingActivities;

    private Double relativeToBestThreshold = 0.05;

    private HashMap<String, CausalNetNode> nodes;
    private HashMap<String, Pair<Double, String>> bestInput;
    private HashMap<String, Pair<Double, String>> bestOutput;

    public CausalNetGenerator(HashMap<String, Double> dActivities, HashMap<Pair<String, String>, Double> dRelations,
                         Map<String, Integer> startingActivities, Map<String, Integer> finishingActivities) {
        this.activities = dActivities;
        this.relations = dRelations;

        this.startingActivities = startingActivities;
        this.finishingActivities = finishingActivities;
    }

    public CausalNet generateModel(Double dependencyThreshold, Double positiveObservations, Double andThreshold) {

        populateFields();
        updateStartsEnds(startingActivities, finishingActivities, positiveObservations);

        CausalNet model = new CausalNet("mined model");
        nodes = new HashMap<>();

        // add all the nodes
        for (String event : activityFrequency.keySet()) {
//			Double observations = activityFrequency.get(event);
            // check that the activity is still alive
            CausalNetNode n = new CausalNetNode(event);
            model.addNode(n);
            nodes.put(event, n);
        }

        HashMap<String, HashSet<Pair<String, String>>> andSplitRelations = getAndSplits(dependencyThreshold, positiveObservations, andThreshold);
        HashMap<String, HashSet<Pair<String, String>>> andJoinRelations = getAndJoins(dependencyThreshold, positiveObservations, andThreshold);

        // add all the and splits
        for (String split : andSplitRelations.keySet()) {
            HashSet<Pair<String, String>> branches = andSplitRelations.get(split);
            for(Pair<String, String> b : branches) {
                HashSet<CausalNetNode> brancheNodes = new HashSet<>();
                brancheNodes.add(nodes.get(b.first()));
                brancheNodes.add(nodes.get(b.second()));
                CausalNetNode[] dests = new CausalNetNode[brancheNodes.size()];
                brancheNodes.toArray(dests);
                addAndSplit(model, nodes.get(split), dests);
            }
        }

        // add all the and joins
        for (String join : andJoinRelations.keySet()) {
            HashSet<Pair<String, String>> branches = andJoinRelations.get(join);
            for(Pair<String, String> b : branches) {
                HashSet<CausalNetNode> brancheNodes = new HashSet<CausalNetNode>();
                brancheNodes.add(nodes.get(b.first()));
                brancheNodes.add(nodes.get(b.second()));
                CausalNetNode[] sources = new CausalNetNode[brancheNodes.size()];
                brancheNodes.toArray(sources);
                addAndJoin(model, nodes.get(join), sources);
            }
        }

        // add the other edges
        for (Pair<String, String> edge : this.relations.keySet()) {
            if (allowedEdge(edge.first(), edge.second(), dependencyThreshold, positiveObservations) /*&&
					!edge.first().equals(edge.second())*/) {

                CausalNetNode A = nodes.get(edge.first());
                CausalNetNode B = nodes.get(edge.second());

                if (A != null && B != null) {
                    if (andSplitRelations.containsKey(A.getLabel()) &&
                            inOnePair(andSplitRelations.get(A.getLabel()), B.getLabel())) {
                        // this is already part of and-split
                    } else if (andJoinRelations.containsKey(B.getLabel()) &&
                            inOnePair(andJoinRelations.get(B.getLabel()), A.getLabel())) {
                        // this is already part of and-join
                    } else if (!inAndRelations(A, B, andSplitRelations.values())) { //&& allowedDependency(edge, dependencyThreshold, activityThreshold)) {
                        // we add the edge A -> B there actually is this dependency
                        addConnection(model, A, B);
                    }
                }
            }
        }

        // add the artificial start and end node to the model
        CausalNetNode start = new CausalNetNode(ARTIFICIAL_START_NAME);
        model.addNode(start);
        CausalNetNode end = new CausalNetNode(ARTIFICIAL_END_NAME);
        model.addNode(end);

        model.setStartNode(start);
        model.setEndNode(end);

        // add artificial start and end connections
        String nodeWithLessOutputs = "";
        int counterNodeWithLessOutput = 10000;
        for (CausalNetNode n : model.getNodes()) {
            if (!n.getLabel().equals(ARTIFICIAL_START_NAME) &&
                    !n.getLabel().equals(ARTIFICIAL_END_NAME)) {
                // no input ----------------------------------------------------
                if (model.getInputBindings(n).isEmpty() || startEvents.contains(n.getLabel())) {
                    // no input event, it's a start
                    addConnection(model, start, n);
                }
                // no output ---------------------------------------------------
                if (model.getOutputBindings(n).isEmpty() || endEvents.contains(n.getLabel())) {
                    // no output, it's an end
                    addConnection(model, n, end);
                }
                if (model.getOutputBindings(n).size() == 1) {
                    CausalNetBinding binding = model.getOutputBindings(n).iterator().next();
                    if (binding.getBoundNodes().contains(n)) {
                        // the only input event is a self loop
                        addConnection(model, n, end);
                    }
                }
                // update the model with less output
                int currentNumberOutputs = model.getOutputBindings(n).size();
                if (currentNumberOutputs < counterNodeWithLessOutput) {
                    counterNodeWithLessOutput = currentNumberOutputs;
                    nodeWithLessOutputs = n.getLabel();
                }
            }
        }
        // no output set, add an artificial one
        if (model.getInputBindings(end).size() == 0) { addConnection(model, nodes.get(nodeWithLessOutputs), end); }

        return model;
    }

    private void addConnection(CausalNet model, CausalNetNode source, CausalNetNode target) {
        if (model != null && source != null && target != null) {
            model.addOutputBinding(source, target);
            model.addInputBinding(target, source);
        }
    }

    private void addAndJoin(CausalNet model, CausalNetNode join, CausalNetNode... branches) {
        model.addInputBinding(join, branches);
        for (CausalNetNode branch : branches) {  model.addOutputBinding(branch, join); }
    }

    private void addAndSplit(CausalNet model, CausalNetNode split, CausalNetNode... branches) {
        model.addOutputBinding(split, branches);
        for (CausalNetNode branch : branches) { model.addInputBinding(branch, split); }
    }

    private void populateFields() {
        this.activityFrequency = new HashMap<>();
        this.bestInput = new HashMap<>();
        this.bestOutput = new HashMap<>();

        Set<String> activities = this.activities.keySet();
        activities.forEach(activity -> activityFrequency.put(activity, this.activities.get(activity)));

        Set<Pair<String, String>> relations = this.relations.keySet();
        for (Pair<String, String> r : relations) {
            Double value = calculateDependencyMeasure(r.first(), r.second());

            if (!r.first().equals(r.second())) {

                // update best input
                if (bestInput.containsKey(r.second())) {
                    Double currentBest = bestInput.get(r.second()).first();
                    if (currentBest < value) {
                        bestInput.put(r.second(), new Pair<>(value, r.first()));
                    }
                } else { bestInput.put(r.second(), new Pair<>(value, r.first())); }

                // update best output
                if (bestOutput.containsKey(r.first())) {
                    Double currentBest = bestOutput.get(r.first()).first();
                    if (currentBest < value) {
                        bestOutput.put(r.first(), new Pair<>(value, r.second()));
                    }
                } else {
                    bestOutput.put(r.first(), new Pair<>(value, r.second()));
                }
            }
        }
    }

    private HashMap<String, HashSet<Pair<String, String>>> getAndSplits(Double dependencyThreshold, Double positiveObservationsThreshold, Double andThreshold) {
        HashMap<String, HashSet<Pair<String, String>>> andSplitRelations = new HashMap<String, HashSet<Pair<String, String>>>();
        for (String split : activityFrequency.keySet()) {
            for (String branch1 : activityFrequency.keySet()) {

                if (!split.equals(branch1) &&
                        allowedEdge(split, branch1, dependencyThreshold, positiveObservationsThreshold)) {
                    for (String branch2 : activityFrequency.keySet()) {

                        if (!split.equals(branch2) && !branch1.equals(branch2) &&
                                allowedEdge(split, branch2, dependencyThreshold, positiveObservationsThreshold)) {

                            Double num = (getRelationsCount(branch1, branch2) + getRelationsCount(branch2, branch1));
                            Double den = (getRelationsCount(split, branch1) + getRelationsCount(split, branch2) + 1);
                            Double andMeasure = num / den;

                            if (andMeasure >= andThreshold) {
                                // the two are actually in and
                                HashSet<Pair<String, String>> ands = andSplitRelations.get(split);
                                if (ands == null) {
                                    ands = new HashSet<Pair<String, String>>();
                                }
                                if (!pairContained(ands, branch1, branch2)) {
                                    Pair<String, String> p = new Pair<>(branch1, branch2);
                                    ands.add(p);
                                    andSplitRelations.put(split, ands);
                                }
                            }
                        }
                    }
                }
            }
        }
        return andSplitRelations;
    }

    private HashMap<String, HashSet<Pair<String, String>>> getAndJoins(Double dependencyThreshold, Double positiveObservationsThreshold, Double andThreshold) {
        HashMap<String, HashSet<Pair<String, String>>> andJoinRelations = new HashMap<String, HashSet<Pair<String, String>>>();
        for (String join : activityFrequency.keySet()) {
            for (String branch1 : activityFrequency.keySet()) {

                if (!join.equals(branch1) &&
                        allowedEdge(branch1, join, dependencyThreshold, positiveObservationsThreshold)) {
                    for (String branch2 : activityFrequency.keySet()) {

                        if (!join.equals(branch1) && !branch1.equals(branch2) &&
                                allowedEdge(branch2, join, dependencyThreshold, positiveObservationsThreshold)) {

                            Double num = (getRelationsCount(branch1, branch2) + getRelationsCount(branch2, branch1));
                            Double den = (getRelationsCount(branch1, join) + getRelationsCount(branch2, join) + 1);
                            Double andMeasure = num / den;

                            if (andMeasure >= andThreshold) {

                                // the two are actually in and
                                HashSet<Pair<String, String>> ands = andJoinRelations.get(join);
                                if (ands == null) {
                                    ands = new HashSet<Pair<String, String>>();
                                }
                                if (!pairContained(ands, branch1, branch2)) {
                                    Pair<String, String> p = new Pair<>(branch1, branch2);
                                    ands.add(p);
                                    andJoinRelations.put(join, ands);
                                }
                            }
                        }
                    }
                }
            }
        }
        return andJoinRelations;
    }

    private Boolean inAndRelations(CausalNetNode A, CausalNetNode B, Collection<? extends Set<Pair<String, String>>> andSplitRelations) {
        for (Set<Pair<String, String>> branchesSet : andSplitRelations) {
            if (pairContained(branchesSet, A.getLabel(), B.getLabel())) {
                return true;
            }
        }
        return false;
    }

    private Boolean pairContained(Set<Pair<String, String>> collection, String A, String B) {
        Pair<String, String> f = new Pair<>(A, B);
        Pair<String, String> s = new Pair<>(B, A);
        if (collection.contains(f) || collection.contains(s)) {
            return true;
        }
        return false;
    }

    private Boolean inOnePair(Set<Pair<String, String>> collection, String A) {
        for (Pair<String, String> p : collection) {
            if (p.first().equals(A) || p.second().equals(A)) {
                return true;
            }
        }
        return false;
    }

    private Double getRelationsCount(String A, String B) {
        Double AB = relations.get(new Pair<>(A, B));
        Double countAB = (AB == null)? 0 : AB;
        return countAB;
    }

    public void updateStartsEnds(Map<String, Integer> startObservations, Map<String, Integer> endObservations, double activityThreshold) {
        this.startEvents = new HashSet<String>(1);
        this.endEvents = new HashSet<String>(1);
        for (String k : startObservations.keySet()) {
            if (startObservations.get(k) > activityThreshold && activities.containsKey(k) && activityFrequency.get(k) > activityThreshold) {
                startEvents.add(k);
            }
        }
        for (String k : endObservations.keySet()) {
            if (endObservations.get(k) > activityThreshold && activities.containsKey(k) && activityFrequency.get(k) > activityThreshold) {
                endEvents.add(k);
            }
        }
    }

    private Boolean allowedEdge(String source, String destination, Double dependencyThreshold, Double positiveObservations) {
        Double edgeMeasure = calculateDependencyMeasure(source, destination);
        return (edgeMeasure >= dependencyThreshold &&
                (bestOutput.get(source).first() - edgeMeasure <= relativeToBestThreshold) &&
                relations.get(new Pair<>(source, destination)) >= positiveObservations);
    }

    private Double calculateDependencyMeasure(String A, String B) {
        Double AB = relations.get(new Pair<>(A, B));
        Double BA = relations.get(new Pair<>(B, A));

        Double countAB = (AB == null)? 0 : AB;
        Double countBA = (BA == null)? 0 : BA;

        Double measure = 0.0;
        measure = (countAB - countBA) / (countAB + countBA + 1);
        return measure;
    }
}
