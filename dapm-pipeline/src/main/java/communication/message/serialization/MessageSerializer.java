package communication.message.serialization;
import communication.message.impl.Alignment;
import communication.message.impl.Trace;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;
import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;

import java.util.Collection;

// TODO: make serialization its own class? Sure it adds an extra step but it will make maintainability easier, and enforce the correct formatting. Should be considered if serialization format changes frequently.
/** Class for serializing Message's. Note that any given instance of this class only is safe to use in a synchronous context. */
public class MessageSerializer implements MessageVisitor<String> {
    private String serialization;

    public String getSerialization() { return serialization; }

    @Override
    public String visit(Event event) {
        this.serialization = "event:" + "{\"traces\": [{" +
                                            "\"attrs\": {\"concept:name\": \"" + event.getCaseID() + "\"}, " +
                                            "\"events\": [" + toJXES(event) + "]}]}";
        return getSerialization();
    }

    /** converts a PetriNet to a PNML string, based on ISO/IEC 15909-2; in particular "A primer on the Petri Net Markup Language and ISO/IEC 15909-2" by Kindler et al.
     * Note that serializations from this method will only include the necessary components of a (single) petri net. That is, it includes the petri net's places with their marking,
     * the transitions, and the arcs. */
    @Override
    public String visit(PetriNet petriNet) {
        this.serialization = petriNet.getName() + ":" + ToPNML(petriNet);
        return getSerialization();
    }

    @Override
    public String visit(Trace trace) {
        this.serialization = trace.getName() + ":" + "{\"traces\": [" + toJXES(trace) +"]}";
        return getSerialization();
    }

    /** Serializes an alignment into a JXES-formatted string such that the resulting JXES contains two traces:
     *  the first one being the log trace, and the second one being the model trace. */
    @Override
    public String visit(Alignment alignment) {
        this.serialization = alignment.getName() + ":" + "{\"traces\": ["
                + toJXES(alignment.getLogTrace()) + ", "
                + toJXES(alignment.getModelTrace())
                + "]}";
        return getSerialization();
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

        return "{\"attrs\": {\"concept:name\": \"" + trace.getCaseID() + "\"}, " +
                    "\"events\": " + sb.toString() +"}";
    }

    private String toJXES(Event event) {
        // TODO: append event attributes, i.e. the collection of additional (non-mining) attributes
        return "{\"concept:name\": \"" + event.getActivity() +
                "\", \"date\": \"" + event.getTimestamp() +
                "\"" + commaSeparatedAttributesString(event.getAttributes()) + '}';
    }


    private String commaSeparatedAttributesString(Collection<Attribute<?>> attributes) {
        if (attributes.isEmpty()) {return "";}
        StringBuilder sb = new StringBuilder();
        for (Attribute<?> attr : attributes) {
            sb.append(", ").append(attr.toString());
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

    public String serializePlace(Place p) {
        return "<place id=\""
                + p.getID()
                + "\"><initialMarking><text>"
                + p.getMarking()
                + "</text></initialMarking></place>";
    }

    public String serializeTransition(Transition t) {
        return "<transition id=\"" + t.getID() + "\"></transition>";
    }

    public String serializeArc(Arc a) {
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
