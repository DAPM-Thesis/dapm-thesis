package communication.message.impl;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.ProcessMapDeserializationStrategy;
import utils.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = ProcessMapDeserializationStrategy.class)
public class ProcessMap extends Message {
    private final Map<String, Pair<Double, Double>> activities = new HashMap<>();
    private final Map<Pair<String, String>, Pair<Double, Double>> relations = new HashMap<>();
    private Set<String> startingActivities = new HashSet<>();
    private Set<String> endingActivities = new HashSet<>();

    public Set<String> getActivities() { return new HashSet<>(activities.keySet()); }
    public Double getActivityRelativeFrequency(String activity) { return (activities.containsKey(activity) ? activities.get(activity).first() : 0.0); }
    public Double getActivityAbsoluteFrequency(String activity) { return (activities.containsKey(activity) ? activities.get(activity).second() : 0.0); }
    public Set<Pair<String, String>> getRelations() { return relations.keySet(); }
    public Double getRelationRelativeFrequency(Pair<String, String> relation) { return (relations.containsKey(relation) ? relations.get(relation).first() : 0.0); }
    public Double getRelationAbsoluteFrequency(Pair<String, String> relation) { return (relations.containsKey(relation) ? relations.get(relation).second() : 0.0); }
    public Set<String> getStartingActivities() { return new HashSet<>(startingActivities); }
    public Set<String> getEndingActivities() { return new HashSet<>(endingActivities); }

    public void addActivity(String activityName, double relativeFrequency, double absoluteFrequency)
        { activities.put(activityName, new Pair<>(relativeFrequency, absoluteFrequency)); }

    public void addStartingActivity(String activityName) { startingActivities.add(activityName); }

    public void addEndingActivity(String activityName) { endingActivities.add(activityName); }

    public void removeActivity(String activityName) { activities.remove(activityName); }

    public void addRelation(String activitySource, String activityTarget, double relativeFrequency, double absoluteFrequency)
        { relations.put(new Pair<>(activitySource, activityTarget), new Pair<>(relativeFrequency, absoluteFrequency)); }

    public boolean isStartActivity(String candidate) {
        return getIncomingActivities(candidate).isEmpty() || startingActivities.contains(candidate);
    }

    public boolean isEndActivity(String candidate) {
        return getOutgoingActivities(candidate).isEmpty() || endingActivities.contains(candidate);
    }

    private Set<String> getIncomingActivities(String candidate) {
        return relations.keySet().stream()
                .map(Pair::second)
                .filter(target -> target.equals(candidate))
                .collect(Collectors.toSet());
    }

    private Set<String> getOutgoingActivities(String candidate) {
        return relations.keySet().stream()
                .map(Pair::first)
                .filter(source -> source.equals(candidate))
                .collect(Collectors.toSet());
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ProcessMap otherMap)) return false;
        return otherMap.activities.equals(activities)
                && otherMap.relations.equals(relations)
                && otherMap.startingActivities.equals(startingActivities)
                && otherMap.endingActivities.equals(endingActivities);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(activities, relations, startingActivities, endingActivities);
    }
}
