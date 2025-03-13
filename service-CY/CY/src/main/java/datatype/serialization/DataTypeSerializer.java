package datatype.serialization;
import datatype.DataMap;
import datatype.event.Attribute;
import datatype.event.Event;
import datatype.petrinet.PetriNet;
import datatype.petrinet.Place;
import datatype.petrinet.Transition;
import datatype.petrinet.arc.Arc;
import datatype.petrinet.arc.PlaceToTransitionArc;
import datatype.petrinet.arc.TransitionToPlaceArc;

import java.util.Collection;

// TODO: make serialization its own class? Sure it adds an extra step but it will make maintainability easier, and enforce the correct formatting. Should be considered if serialization format changes frequently.
public class DataTypeSerializer implements DataTypeVisitor<String> {
    private String serialization;

    public String getSerialization() { return serialization; }

    @Override
    public String visit(Event e) {
        this.serialization = e.getName() + ":" + toJXES(e);
        return getSerialization();
    }

    /** converts a PetriNet to a PNML string, based on ISO/IEC 15909-2; in particular "A primer on the Petri Net Markup Language and ISO/IEC 15909-2" by Kindler et al.
     * Note that serializations from this method will only include the necessary components of a (single) petri net. That is, it includes the petri net's places with their marking,
     * the transitions, and the arcs. */
    @Override
    public String visit(PetriNet pn) {
        this.serialization = pn.getName() + ":" + ToPNML(pn);
        return getSerialization();
    }

    @Override
    public String visit(DataMap dm) {
        this.serialization = dm.getName() + ":" + toJSON(dm);
    }

    private String toJSON(DataMap dm) {
        // TODO: implement
        throw new IllegalStateException("Not implemented yet");
    }

    private String toJXES(Event e) {
        // TODO: append event attributes, i.e. the collection of additional (non-mining) attributes
        return "{\"traces\": [{" +
                "\"attrs\": {\"concept:name\": \"" + e.getCaseID() + "\"}, " +
                "\"events\": [{" +
                "\"concept:name\": \"" + e.getActivity() +
                "\", \"date\": \"" + e.getTimestamp() +
                "\", " + commaSeparatedAttributesString(e.getAttributes()) + "}]}]}";
    }

    private String commaSeparatedAttributesString(Collection<Attribute<?>> attributes) {
        if (attributes.isEmpty()) {return "";}
        StringBuilder sb = new StringBuilder();
        for (Attribute<?> attr : attributes) {
            sb.append(attr.toString()).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String ToPNML(PetriNet pn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pnml xmlns=\"https://www.pnml.org/version-2009/version-2009.php\">")
                .append("<net id=\"pn\" type=\"https://orbit.dtu.dk/en/publications/a-primer-on-the-petri-net-markup-language-and-isoiec-15909-2\">")
                .append("<page id=\"top-level\"><name><text>Petri Net name</name></text></page>");

        for (Place p : pn.getPlaces()) { sb.append(serializePlace(p)); }
        for (Transition t : pn.getTransitions()) { sb.append(serializeTransition(t)); }
        for (Arc a : pn.getFlowRelation()) { sb.append(serializeArc(a)); }

        sb.append("</page></net></pnml>");

        String pnmlString = sb.toString();
        System.out.println(pnmlString);
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
