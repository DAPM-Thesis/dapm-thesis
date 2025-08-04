package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.causalnet.CausalNetNode;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.parsing.JSONParser;

import java.util.*;
import java.util.stream.Collectors;

public class CausalNetDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jsonMap = (Map<String, Object>) (new JSONParser()).parse(payload);
        assert jsonMap.containsKey("label"): "All CausalNets must have a label";
        CausalNet causalNet = new CausalNet((String) jsonMap.get("label"));

        parseNodes(causalNet, jsonMap);
        parseInputBindings(causalNet, jsonMap);
        parseOutputBindings(causalNet, jsonMap);

        if (jsonMap.containsKey("start")) { causalNet.setStartNode(new CausalNetNode((String) jsonMap.get("start"))); }
        if (jsonMap.containsKey("end")) { causalNet.setEndNode(new CausalNetNode((String) jsonMap.get("end"))); }

        return causalNet;
    }

    private void parseInputBindings(CausalNet causalNet, Map<String, Object> jsonMap) {
        if (!jsonMap.containsKey("inputBindings"))
            { return; }
        ArrayList<Map<String, Object>> inputBindings = (ArrayList<Map<String, Object>>) jsonMap.get("inputBindings");
        for (Map<String, Object> binding : inputBindings) {
            CausalNetNode origin = new CausalNetNode((String) binding.get("node"));
            Collection<CausalNetNode> boundNodes = ((ArrayList<String>) binding.get("boundNodes")).stream()
                    .map(CausalNetNode::new)
                    .collect(Collectors.toSet());
            if (!boundNodes.isEmpty()) { causalNet.addInputBinding(origin, boundNodes); }
        }
    }

    private void parseOutputBindings(CausalNet causalNet, Map<String, Object> jsonMap) {
        if (!jsonMap.containsKey("outputBindings"))
            { return; }

        ArrayList<Map<String, Object>> outputBindings = (ArrayList<Map<String, Object>>) jsonMap.get("outputBindings");
        for (Map<String, Object> binding : outputBindings) {
            CausalNetNode origin = new CausalNetNode((String) binding.get("node"));
            Collection<CausalNetNode> boundNodes = ((ArrayList<String>) binding.get("boundNodes")).stream()
                    .map(CausalNetNode::new)
                    .collect(Collectors.toSet());
            if (!boundNodes.isEmpty()) { causalNet.addOutputBinding(origin, boundNodes); }
        }
    }

    private void parseNodes(CausalNet causalNet, Map<String, Object> jsonMap) {
        if (!jsonMap.containsKey("nodes"))
            { return; }
        ((ArrayList<String>) jsonMap.get("nodes")).forEach(label -> causalNet.addNode(new CausalNetNode(label)));
    }
}
