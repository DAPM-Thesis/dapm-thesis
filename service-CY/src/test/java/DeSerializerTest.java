
import datatype.DeSerializer;
import datatype.event.Attribute;
import datatype.event.Event;
import datatype.petrinet.PetriNet;
import datatype.petrinet.Place;
import datatype.petrinet.Transition;
import datatype.petrinet.arc.Arc;
import datatype.petrinet.arc.PlaceToTransitionArc;
import datatype.petrinet.arc.TransitionToPlaceArc;
import datatype.serialization.DataTypeSerializer;
import datatype.DataType;
import datatype.DeSerializer;
import datatype.serialization.deserialization.DataTypeFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
        DataTypeSerializer serializer = new DataTypeSerializer();
        PetriNet pn = getPetriNetExample();
        String pnml = serializer.visit(pn);
        PetriNet pn_2 = DeSerializer.PNMLToPetriNet(pnml); // TODO: update pnml deserialization in all tests to use new deserialization

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

        String XMLPathString = "src/test/resources/pnml/article.xml";
        String XMLContents = Files.readString(Paths.get(XMLPathString));
        PetriNet pn_2 = DeSerializer.PNMLToPetriNet(XMLContents);

        assertEquals(pn, pn_2);
    }

    @Test
    void testDeserializeSingleEventJXES() {
        // make event
        String caseID = "id1";
        String activity = "a1";
        String timestamp = "15:07Z";
        Attribute<Integer> attr1 = new Attribute<>("int", 5);
        Attribute<List<Double>> attr2 = new Attribute<>("listAttr", Arrays.asList(5.0, 0.0));
        Set<Attribute<?>> extraAttrs = new HashSet<>(Arrays.asList(attr1, attr2));
        Event event = new Event(caseID, activity, timestamp, extraAttrs);

        // recreate event by serializing and thn deserializing
        DataTypeSerializer serializer = new DataTypeSerializer();
        String JXES = serializer.visit(event);
        System.out.println(JXES);
        Event event_2 = (Event) DataTypeFactory.deserialize(JXES);
        assertEquals(event.getName(), event_2.getName());
        assertEquals(event.getActivity(), event_2.getActivity());
        assertEquals(event.getTimestamp(), event_2.getTimestamp());
        assertEquals(event.getAttributes(), event_2.getAttributes());
        assertEquals(event, event_2);

    }

    @Test
    void testDeserializeEventExample() throws IOException {
        String JXESPathString = "src/test/resources/jxes_example.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        Event e = new Event("","","",new HashSet<>());
        e.getDeserializationStrategy().deserialize(JXESContents);
    }

}