import main.datatype.petrinet.PetriNet;
import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;
import main.datatype.petrinet.arc.Arc;
import main.datatype.petrinet.arc.PlaceToTransitionArc;
import main.datatype.petrinet.arc.TransitionToPlaceArc;
import main.datatype.visitorpattern.SerializerVisitor;
import main.datatype.DeSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeSerializerTest {

    public PetriNet getPetriNetExample() {
        Place p1 = new Place("p1", 0);
        Place p2 = new Place("p2", 0);
        Place p3 = new Place("p3", 0);
        Place p4 = new Place("p4", 0);
        Transition t1 = new Transition("t1");
        Transition t2 = new Transition("t2");
        PlaceToTransitionArc a1 = new PlaceToTransitionArc("a1", p1, t1);
        TransitionToPlaceArc a2_1 = new TransitionToPlaceArc("a2_1", t1, p2);
        TransitionToPlaceArc a2_2 = new TransitionToPlaceArc("a2_2", t1, p3);
        PlaceToTransitionArc a3_1 = new PlaceToTransitionArc("a3_1", p2, t2);
        PlaceToTransitionArc a3_2 = new PlaceToTransitionArc("a3_2", p3, t2);
        TransitionToPlaceArc a4 = new TransitionToPlaceArc("a4", t2, p4);

        Set<Place> places = new HashSet<>(Arrays.asList(p1,p2,p3,p4));
        Set<Transition> transitions = new HashSet<>(Arrays.asList(t1, t2));
        Set<Arc> flowRelation = new HashSet<>(Arrays.asList(a1,a2_1,a2_2, a3_1, a3_2, a4));
        // Note that firing every transition when enabled in this petri net results in 2 tokens in p4 at the end
        // so this is not a workflow net
        return new PetriNet(places, transitions, flowRelation);
    }

    @Test
    void testDeserializePNML() {
        SerializerVisitor serializer = new SerializerVisitor();
        PetriNet pn = getPetriNetExample();
        String pnml = serializer.visitPetriNet(pn);
        PetriNet pn_2 = DeSerializer.PNMLToPetriNet(pnml);

        assertEquals(pn, pn_2);
    }

    @Test
    void testDeserializePetriNetExample() throws IOException {
        PetriNet pn = new PetriNet();
        Place p1 = new Place("p1", 3);
        pn.addPlace(p1);
        Transition t1 = new Transition("t1");
        pn.addTransition(t1);
        pn.addArc(new PlaceToTransitionArc("a1", p1, t1));

        String XMLPathString = "service-CY/CY/src/test/resources/pnml/article.xml";
        String XMLContents = Files.readString(Paths.get(XMLPathString));
        PetriNet pn_2 = DeSerializer.PNMLToPetriNet(XMLContents);

        assertEquals(pn, pn_2);
    }
}