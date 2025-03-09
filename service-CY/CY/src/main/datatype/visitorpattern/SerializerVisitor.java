package main.datatype.visitorpattern;

import main.datatype.Event;
import main.datatype.petrinet.PetriNet;
import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;
import main.datatype.petrinet.arc.Arc;
import main.datatype.petrinet.arc.PlaceToTransitionArc;
import main.datatype.petrinet.arc.TransitionToPlaceArc;

public class SerializerVisitor implements Visitor<String> {
    @Override
    public String visitEvent(Event e) {
        return "";
    }

    /** converts a PetriNet to a PNML string, based on ISO/IEC 15909-2; in particular "A primer on the Petri Net Markup Language and ISO/IEC 15909-2" by Kindler et al.
     * Note that serializations from this method will only include the necessary components of a (single) petri net. That is, it includes the petri net's places with their marking,
     * the transitions, and the arcs. */
    @Override
    public String visitPetriNet(PetriNet pn) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pnml xmlns=\"https://www.pnml.org/version-2009/version-2009.php\">")
                .append("<net id=\"pn\" type=\"https://orbit.dtu.dk/en/publications/a-primer-on-the-petri-net-markup-language-and-isoiec-15909-2\">")
                .append("<page id=\"top-level\"><name><text>Petri Net name</name></text></page>");

        for (Place p : pn.getPlaces()) { sb.append(serializePlace(p)); }
        for (Transition t : pn.getTransitions()) { sb.append(serializeTransition(t)); }
        for (Arc a : pn.getFlowRelation()) { sb.append(serializeTransition(a)); }

        sb.append("</page>")
                .append("</net>")
                .append("</pnml>");

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

    public String serializeTransition(Arc a) {
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
