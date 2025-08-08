package communication.message.serialization;
import communication.message.Message;
import communication.message.impl.*;
import communication.message.impl.causalnet.CausalNet;
import communication.message.impl.causalnet.CausalNetBinding;
import communication.message.impl.causalnet.CausalNetNode;
import communication.message.impl.softconformance.SoftConformanceReport;
import communication.message.impl.softconformance.models.SoftConformanceStatus;
import communication.message.impl.softconformance.models.pdfa.PDFA;
import communication.message.impl.softconformance.models.pdfa.PDFAEdge;
import communication.message.impl.softconformance.models.pdfa.PDFANode;
import communication.message.impl.time.UTCTime;
import communication.message.impl.time.Date;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import communication.message.serialization.parsing.JSONParser;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import utils.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Stream;

/** Class for serializing Message's. Note that any given instance of this class only is safe to use in a synchronous context. */
public class MessageSerializer implements MessageVisitor<String> {
    private String serialization;

    public String getSerialization() { return serialization; }

    @Override
    public String visit(Event event) {
        this.serialization = event.getName() + ':' + "{\"traces\": [{" +
                                            "\"attrs\": {\"concept:name\": " + JSONParser.toJSONString(event.getCaseID()) + "}, " +
                                            "\"events\": [" + toJXES(event) + "]}]}";
        return getSerialization();
    }

    /** converts a PetriNet to a PNML string, based on ISO/IEC 15909-2; in particular "A primer on the Petri Net Markup Language and ISO/IEC 15909-2" by Kindler et al.
     * Note that serializations from this method will only include the necessary components of a (single) petri net. That is, it includes the petri net's places with their marking,
     * the transitions, and the arcs. */
    @Override
    public String visit(PetriNet petriNet) {
        this.serialization = petriNet.getName() + ':' + ToPNML(petriNet);
        return getSerialization();
    }

    @Override
    public String visit(Trace trace) {
        this.serialization = trace.getName() + ':' + "{\"traces\": [" + toJXES(trace) +"]}";
        return getSerialization();
    }

    /** Serializes an alignment into a JXES-formatted string such that the resulting JXES contains two traces:
     *  the first one being the log trace, and the second one being the model trace. */
    @Override
    public String visit(Alignment alignment) {
        this.serialization = alignment.getName() + ':' + "{\"traces\": ["
                + toJXES(alignment.getLogTrace()) + ", "
                + toJXES(alignment.getModelTrace())
                + "]}";
        return getSerialization();
    }

    @Override
    public String visit(Date time) {
        this.serialization = time.getName() + ':' + time.getTime().toString();
        return getSerialization();
    }

    @Override
    public String visit(UTCTime UTCTime) {
        this.serialization = UTCTime.getName() + ':' + UTCTime.getTime().toString();
        return getSerialization();
    }

    @Override
    public String visit(Metrics metrics) {
        this.serialization = metrics.getName() + ':' + metrics;
        return getSerialization();
    }

    @Override
    public String visit(ProcessMap processMap) {
        this.serialization = processMap.getName() + ':' + serialize(processMap);
        return getSerialization();
    }

    @Override
    public String visit(CausalNet causalNet) {
        this.serialization = causalNet.getName() + ':' + serialize(causalNet);
        return getSerialization();
    }

    @Override
    public String visit(SoftConformanceReport softConformanceReport) {
        this.serialization = softConformanceReport.getName() + ':' + serialize(softConformanceReport);
        return getSerialization();
    }

    private String serialize(SoftConformanceReport softConformanceReport) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (!softConformanceReport.isEmpty()) {
            softConformanceReport.forEach((key, value) ->
                            sb.append('\"').append(key).append('\"')
                                    .append(':').append(serialize(value)).append(','));
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private String serialize(SoftConformanceStatus status) {
        String lastAct = (status.getLastAct() == null) ? null : '\"' + status.getLastAct() + '\"';
        return "{" +
                "\"model\": " + serialize(status.getModel()) + ", " +
                "\"caseID\": \"" + status.getCaseID() + "\", " +
                "\"lastAct\": " + lastAct + ", " +
                "\"lastProb\": " + status.getLastProbability() + ", " +
                "\"prob\": " + status.getSequenceProbability() + ", " +
                "\"logProb\": \"" + status.getSequenceLogProbability() + "\", " +
                "\"mean\": \"" + serialize(status.getMean()) + "\", " +
                "\"lastUpdate\": " + status.getLastUpdateValue() +
                "}";
    }

    // Mean implements Serializable and we therefore just serialize into its base64 representation
    private String serialize(Mean mean) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(byteStream)) {
            oos.writeObject(mean);
            return Base64.getEncoder().encodeToString(byteStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String serialize(PDFA model) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"nodes\": [");
        if (!model.getNodes().isEmpty()) {
            model.getNodes().forEach(node -> sb.append("\"").append(node.label()).append("\","));
            sb.setLength(sb.length() - 1);
        }
        sb.append("],");

        sb.append("\"edges\": [");
        if (!model.getEdges().isEmpty()) {
            model.getEdges().forEach(edge -> sb.append(edge.serialize()).append(","));
            sb.setLength(sb.length() - 1);
        }
        sb.append("],");

        sb.append("\"inEdgeMap\": ");
        serializeEdgeMap(model.getInEdgeMap(), sb);
        sb.append(",\"outEdgeMap\": ");
        serializeEdgeMap(model.getOutEdgeMap(), sb);

        String attributeName = (model.getAttributeNameUsed() != null) ? '\"' + model.getAttributeNameUsed() + '\"' : "null";
        sb.append(",\"attributeNameUsed\": ").append(attributeName).append(",");
        sb.append("\"weightFactor\": ").append(model.getWeightFactor());
        sb.append("}");
        return sb.toString();
    }

    private void serializeEdgeMap(Map<PDFANode, Collection<PDFAEdge>> edgeMap, StringBuilder sb) {
        sb.append("{");
        for (Map.Entry<PDFANode, Collection<PDFAEdge>> entry : edgeMap.entrySet()) {
            sb.append("\"").append(entry.getKey().label()).append("\": [");
            if (!entry.getValue().isEmpty()) {
                entry.getValue().forEach(edge -> sb.append(edge.serialize()).append(","));
                sb.setLength(sb.length() - 1);
            }
            sb.append("],");
        }
        if (!edgeMap.isEmpty()) { sb.setLength(sb.length() - 1); }
        sb.append("}");
    }


    private String serialize(CausalNet causalNet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"label\":\"").append(causalNet.getLabel()).append("\"");

        serializeCNetNodes(sb, causalNet);
        serializeCNetBindings(sb, causalNet, CausalNetBinding.Type.INPUT);
        serializeCNetBindings(sb, causalNet, CausalNetBinding.Type.OUTPUT);

        if (causalNet.getStart() != null) { sb.append(",\"start\":\"").append(causalNet.getStart().getLabel()).append("\""); }
        if (causalNet.getEnd() != null) { sb.append(",\"end\":\"").append(causalNet.getEnd().getLabel()).append("\""); }

        sb.append("}"); // close outermost object
        return sb.toString();
    }

    private void serializeCNetBindings(StringBuilder sb, CausalNet CNet, CausalNetBinding.Type type) {
        if (type != CausalNetBinding.Type.INPUT && type != CausalNetBinding.Type.OUTPUT) { throw new TypeNotPresentException("The called type does not exist", null); }
        String propertyName = (type == CausalNetBinding.Type.INPUT) ? "inputBindings" : "outputBindings";
        Map<CausalNetNode, Set<CausalNetBinding>> bindings = (type == CausalNetBinding.Type.INPUT) ? CNet.getInputBindings() : CNet.getOutputBindings();

        if (bindings.isEmpty())
            { return; }

        sb.append(",\"").append(propertyName).append("\": [");
        for (Map.Entry<CausalNetNode, Set<CausalNetBinding>> entry : bindings.entrySet()) {
            sb.append("{\"node\": \"").append(entry.getKey().getLabel()).append("\", \"boundNodes\": [");
            if (!entry.getValue().isEmpty()) {
                List<String> boundLabels = new ArrayList<>();
                for (CausalNetBinding binding : entry.getValue()) {
                    for (CausalNetNode node : binding.getBoundNodes()) { boundLabels.add("\"" + node.getLabel() + "\""); }
                }
                sb.append(String.join(",", boundLabels));
            }
            sb.append("]},");
        }
        sb.setCharAt(sb.length()-1, ']');

    }

    private void serializeCNetNodes(StringBuilder sb, CausalNet causalNet) {
        if (!causalNet.getNodes().isEmpty()) {
            sb.append(",\"nodes\": [");
            for (CausalNetNode node : causalNet.getNodes()) {
                sb.append("\"").append(node.getLabel()).append("\"").append(",");
            }
            sb.setCharAt(sb.length() - 1, ']');
        }
    }

    private String serialize(ProcessMap processMap) {
        Map<String, Pair<Double, Double>> activities = new HashMap<>();
        Set<String> activityNames = processMap.getActivities();
        activityNames.forEach(activity -> activities.put(activity, new Pair<>(processMap.getActivityRelativeFrequency(activity), processMap.getActivityAbsoluteFrequency(activity))));

        Map<Pair<String, String>, Pair<Double, Double>> relations = new HashMap<>();
        Set<Pair<String,String>> relationPairs = processMap.getRelations();
        relationPairs.forEach(relation -> relations.put(relation, new Pair<>(processMap.getRelationRelativeFrequency(relation), processMap.getRelationAbsoluteFrequency(relation))));

        Set<String> startingActivities = processMap.getStartingActivities();
        Set<String> endingActivities = processMap.getEndingActivities();

        StringBuilder activitiesBuilder = new StringBuilder();
        if (!activities.isEmpty()) {
            activitiesBuilder.append("\"activities\": {");
            for (Map.Entry<String, Pair<Double, Double>> entry : activities.entrySet()) {
                activitiesBuilder.append("\"").append(entry.getKey()).append("\":{\"relFreq\":").append(entry.getValue().first()).append(", \"absFreq\":").append(entry.getValue().second()).append("},");
            }
            activitiesBuilder.setLength(activitiesBuilder.length() - 1);
            activitiesBuilder.append("}");
        }

        StringBuilder relationsBuilder = new StringBuilder();
        if (!relations.isEmpty()) {
            relationsBuilder.append("\"relations\": {");
            for (Map.Entry<Pair<String, String>, Pair<Double, Double>> entry : relations.entrySet()) {
                String source = entry.getKey().first();
                String target = entry.getKey().second();
                double relativeFrequency = entry.getValue().first();
                double absoluteFrequency = entry.getValue().second();
                relationsBuilder.append("\"").append(source).append("@@@").append(target).append("\":{");
                relationsBuilder.append("\"relFreq\":").append(relativeFrequency).append(", \"absFreq\":").append(absoluteFrequency).append("},");
            }
            relationsBuilder.setLength(relationsBuilder.length() - 1);
            relationsBuilder.append("}");
        }

        StringBuilder startingActivitiesBuilder = new StringBuilder();
        if (!startingActivities.isEmpty()) {
            startingActivitiesBuilder.append("\"startingActivities\": \"");
            for (String startingActivity : startingActivities) {
                startingActivitiesBuilder.append(startingActivity).append("@@@");
            }
            startingActivitiesBuilder.setLength(startingActivitiesBuilder.length() - 3);
            startingActivitiesBuilder.append("\"");
        }

        StringBuilder endingActivitiesBuilder = new StringBuilder();
        if (!endingActivities.isEmpty()) {
            endingActivitiesBuilder.append("\"endingActivities\": \"");
            for (String endingActivity : endingActivities) {
                endingActivitiesBuilder.append(endingActivity).append("@@@");
            }
            endingActivitiesBuilder.setLength(endingActivitiesBuilder.length() - 3);
            endingActivitiesBuilder.append("\"");
        }

        List<String> properties = Stream.of(activitiesBuilder.toString(), relationsBuilder.toString(), startingActivitiesBuilder.toString(), endingActivitiesBuilder.toString())
                .filter(s -> !s.isEmpty())
                .toList();

        return '{' + String.join(",", properties) + '}';
    }

    private String toJXES(Trace trace) {
        assert trace != null && !trace.isEmpty()
                : "Trace is empty. This is currently not supported but may be in the future if relevant";

        StringBuilder sb = new StringBuilder("[");
        for (Event e : trace) {
            sb.append(toJXES(e)).append(", ");
        }
        if (!trace.isEmpty()) { sb.delete(sb.length() - 2, sb.length()); } // delete last ", "
        sb.append(']');

        return "{\"attrs\": {\"concept:name\": " + JSONParser.toJSONString(trace.getCaseID()) + "}, " +
                    "\"events\": " + sb +"}";
    }

    private String toJXES(Event event) {
        return "{\"concept:name\": " + JSONParser.toJSONString(event.getActivity()) +
                ", \"date\": " + JSONParser.toJSONString(event.getTimestamp())
                + commaSeparatedAttributesString(event.getAttributes()) + '}';
    }


    private String commaSeparatedAttributesString(Collection<Attribute<?>> attributes) {
        if (attributes.isEmpty()) {return "";}
        StringBuilder sb = new StringBuilder();
        MessageSerializer serializer = new MessageSerializer();
        for (Attribute<?> attr : attributes) {

            sb.append(", ")
                    .append(JSONParser.toJSONString(attr.getName()))
                    .append(": ");
            if (attr.getValue() instanceof Message message) { sb.append(serializer.visit(message)); }
            else { sb.append(JSONParser.toJSONString(attr.getValue()));}
        }
        return sb.toString();
    }

    private String ToPNML(PetriNet pn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pnml xmlns=\"https://www.pnml.org/version-2009/version-2009.php\">")
                .append("<net id=\"pn\" type=\"https://orbit.dtu.dk/en/publications/a-primer-on-the-petri-net-markup-language-and-isoiec-15909-2\">")
                .append("<page id=\"top-level\"><name><text>Petri Net name</text></name>");

        for (Place p : pn.getPlaces()) { sb.append(serializePlace(p)); }
        for (Transition t : pn.getTransitions()) { sb.append(serializeTransition(t)); }
        for (Arc a : pn.getFlowRelation()) { sb.append(serializeArc(a)); }

        sb.append("</page></net></pnml>");

        String pnmlString = sb.toString();
        // credit to https://www.baeldung.com/java-count-chars for this syntax
        assert pnmlString.chars().filter(ch -> ch == '<').count() == pnmlString.chars().filter(ch -> ch == '>').count()
                : "Not every '<' has a '>' or vice versa.";
        assert pnmlString.chars().filter(ch -> ch == '\"').count() % 2 == 0 : "Not all quotations are closed";
        return pnmlString;
    }

    private String serializePlace(Place p) {
        return "<place id=\""
                + p.getID()
                + "\"><initialMarking><text>"
                + p.getMarking()
                + "</text></initialMarking></place>";
    }

    // allows for calling visit on the superclass. This will then call the correct subclass acceptVisitor (whose serialization should set this class' serialization attribute!)
    public String visit(Message message) {
        message.acceptVisitor(this);
        return getSerialization();
    }

    private String serializeTransition(Transition t) {
        return "<transition id=\"" + t.getID() + "\"></transition>";
    }

    private String serializeArc(Arc a) {
        String source;
        String target;
        if (a instanceof TransitionToPlaceArc tpa) {
            source = tpa.getSource().getID();
            target = tpa.getTarget().getID();
        } else if (a instanceof PlaceToTransitionArc pta) {
            source = pta.getSource().getID();
            target = pta.getTarget().getID();
        } else { throw new IllegalCallerException("arc type not supported. "); }
        return "<arc id=\""
                + a.getID()
                + "\" source=\""
                + source
                + "\" target=\""
                + target
                + "\"></arc>";
    }
}
