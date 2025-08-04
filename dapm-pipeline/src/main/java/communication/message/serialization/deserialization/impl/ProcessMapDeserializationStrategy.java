package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.ProcessMap;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.parsing.JSONParser;
import utils.Pair;

import java.util.*;

public class ProcessMapDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        ProcessMap process = new ProcessMap();
        Map<String, Object> jsonMap = (Map<String, Object>) new JSONParser().parse(payload);

        addActivities(process, jsonMap);
        addRelations(process, jsonMap);
        addStartingActivities(process, jsonMap);
        addEndingActivities(process, jsonMap);

        return process;
    }

    private void addActivities(ProcessMap process, Map<String, Object> jsonMap) {
        Map<String, Map<String, Double>> activities = (Map<String, Map<String, Double>>) jsonMap.getOrDefault("activities", new HashMap<>());
        for (Map.Entry<String, Map<String, Double>> entry : activities.entrySet()) {
            String activityName = entry.getKey();
            double relativeFrequency = entry.getValue().get("relFreq");
            double absoluteFrequency = entry.getValue().get("absFreq");
            process.addActivity(activityName, relativeFrequency, absoluteFrequency);
        }
    }

    private void addRelations(ProcessMap process, Map<String, Object> jsonMap) {
        Map<String, Map<String, Double>> relations = (Map<String, Map<String, Double>>) jsonMap.getOrDefault("relations", new HashMap<>());
        for (Map.Entry<String, Map<String, Double>> entry : relations.entrySet()) {
            String[] sourceAndTarget = entry.getKey().split("@@@");
            String source = sourceAndTarget[0];
            String target = sourceAndTarget[1];
            double relativeFrequency = entry.getValue().get("relFreq");
            double absoluteFrequency = entry.getValue().get("absFreq");
            process.addRelation(source, target, relativeFrequency, absoluteFrequency);
        }
    }

    private void addStartingActivities(ProcessMap process, Map<String, Object> jsonMap) {
        String startingActivitiesStr = (String) jsonMap.getOrDefault("startingActivities", "");
        if (startingActivitiesStr.isEmpty())
            { return; }

        Arrays.stream(startingActivitiesStr.split("@@@"))
                .forEach(process::addStartingActivity);
    }

    private void addEndingActivities(ProcessMap process, Map<String, Object> jsonMap) {
        String endingActivitiesStr = (String) jsonMap.getOrDefault("endingActivities", "");
        if (endingActivitiesStr.isEmpty())
            { return; }

        Arrays.stream(endingActivitiesStr.split("@@@"))
                .forEach(process::addEndingActivity);
    }

}
