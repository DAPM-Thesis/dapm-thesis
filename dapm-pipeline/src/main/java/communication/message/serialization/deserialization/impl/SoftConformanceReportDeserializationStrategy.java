package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.softconformance.models.SoftConformanceStatus;
import communication.message.impl.softconformance.models.pdfa.PDFA;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.parsing.JSONParser;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SoftConformanceReportDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        Map<String, Map<String, Object>> jsonMap = (Map<String, Map<String, Object>>) new JSONParser().parse(payload);

        SoftConformanceReport report = new SoftConformanceReport();
        jsonMap.forEach((key, value) -> report.put(key, parseStatus(value)));

        return report;
    }

    private SoftConformanceStatus parseStatus(Map<String, Object> rawStatus) {
        double prob = (double) rawStatus.get("prob");
        double logProb = deserializeLogProb(rawStatus);
        String caseID = (String) rawStatus.get("caseID");
        Mean mean = deserializeMean(rawStatus);
        long lastUpdate = ((Number) rawStatus.get("lastUpdate")).longValue();
        PDFA model = deserializeModel((Map<String, Object>) rawStatus.get("model"));
        double lastProb = (double) rawStatus.get("lastProb");
        String lastAct = (rawStatus.get("lastAct") == null) ? null : rawStatus.get("lastAct").toString();

        return new SoftConformanceStatus(model, caseID, lastAct, lastProb, prob, logProb, mean, lastUpdate);
    }

    public PDFA deserializeModel(String strModel) {
        return deserializeModel((Map<String, Object>) (new JSONParser()).parse(strModel));
    }

    private PDFA deserializeModel(Map<String, Object> rawModel) {
        PDFA model = new PDFA();

        ((List<String>) rawModel.get("nodes")).forEach(model::addNode);

        List<Map<String, Object>> rawEdges = (List<Map<String, Object>>) rawModel.get("edges");
        rawEdges.forEach(edge ->
                model.addEdge((String) edge.get("from"), (String) edge.get("to"), (Double) edge.get("probability")));

        // inEdgeMap and outEdgeMap will automatically be filled based on addNode() and addEdge()

        String attributeNameUsed = (rawModel.get("attributeNameUsed") == null) ? null : rawModel.get("attributeNameUsed").toString();
        double weightFactor = (double) rawModel.get("weightFactor");

        model.setWeightFactor(weightFactor);
        model.setAttributeNameUsed(attributeNameUsed);
        return model;
    }

    private Mean deserializeMean(Map<String, Object> rawStatus) {
        String base64Mean = (String) rawStatus.get("mean");
        byte[] data = Base64.getDecoder().decode(base64Mean);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (Mean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    private double deserializeLogProb(Map<String, Object> rawStatus) {
        String rawProb = (String) rawStatus.get("logProb");
        return switch (rawProb.toLowerCase()) {
            case "infinity" -> Double.POSITIVE_INFINITY;
            case "-infinity" -> Double.NEGATIVE_INFINITY;
            case "nan" -> Double.NaN;
            default -> Double.parseDouble(rawProb);
        };
    }
}
