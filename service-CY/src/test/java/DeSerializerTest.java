
import datatype.Alignment;
import datatype.DataMap;
import datatype.DataType;
import datatype.Trace;
import datatype.event.Attribute;
import datatype.event.Event;
import datatype.petrinet.PetriNet;
import datatype.petrinet.Place;
import datatype.petrinet.Transition;
import datatype.petrinet.arc.Arc;
import datatype.petrinet.arc.PlaceToTransitionArc;
import datatype.petrinet.arc.TransitionToPlaceArc;
import datatype.serialization.DataTypeSerializer;
import datatype.serialization.deserialization.DataTypeFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DeSerializerTest {

    @Test
    void PNMLInverseTest() {
        DataTypeSerializer serializer = new DataTypeSerializer();
        PetriNet pn = getPetriNetExample();
        String pnml = serializer.visit(pn);
        DataType pn_2 = DataTypeFactory.deserialize(pnml);

        assertEquals(pn, pn_2);
    }

    @Test
    void petriNetTest() throws IOException {
        PetriNet pn = new PetriNet();
        Place p1 = new Place("p1", 3);
        pn.addPlace(p1);
        Transition t1 = new Transition("t1");
        pn.addTransition(t1);
        pn.addArc(new PlaceToTransitionArc("a1", p1, t1));

        String XMLPathString = "src/test/resources/pnml/article.xml";
        String XMLContents = Files.readString(Paths.get(XMLPathString));
        String expected = pn.getName() + ":" + XMLContents;
        DataType pn_2 = DataTypeFactory.deserialize(expected);

        assertEquals(pn, pn_2);
    }

    @Test
    void eventJXESInverseTest() {
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
        Event event_2 = (Event) DataTypeFactory.deserialize(JXES);

        assertEquals(event, event_2);

    }

    @Test
    void articleEventTest() throws IOException {
        String JXESPathString = "src/test/resources/event/jxes_example.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        Event e = new Event("","","",new HashSet<>());
        e.getDeserializationStrategy().deserialize(JXESContents);
    }

    @Test
    void eventCommaActivityNameTest() throws IOException {
        String JXESPathString = "src/test/resources/event/jxes_event_comma_activity_name.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        Event e = new Event("","","",new HashSet<>());
        e.getDeserializationStrategy().deserialize(JXESContents);
    }

    @Test
    void eventQuotationActivityNameTest() throws IOException {
        String JXESPathString = "src/test/resources/event/jxes_event_quotation_activity_name.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        Event e = new Event("random instance to call getDeserializationStrategy() on the above JXES.","","",new HashSet<>());
        e.getDeserializationStrategy().deserialize(JXESContents);
    }

    @Test
    void traceSingleInverseTest() {
        // create trace
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1));
        Trace singletonTrace = new Trace(events);

        // serialize and deserialize
        DataTypeSerializer serializer = new DataTypeSerializer();
        String serialization = serializer.visit(singletonTrace);
        DataType singletonTrace_2 = DataTypeFactory.deserialize(serialization);

        assertEquals(singletonTrace, singletonTrace_2);
    }

    @Test
    void traceMultipleInverseTest() {
        // create trace
        Event e1 = new Event("c1", "a1", "t1", new HashSet<>());
        Event e2 = new Event("c1", "a2", "t2", new HashSet<>());
        Event e3 = new Event("c1", "a3", "t3", new HashSet<>());
        List<Event> events = new ArrayList<>(List.of(e1, e2, e3));
        Trace multipleTrace = new Trace(events);

        // serialize and deserialize
        DataTypeSerializer serializer = new DataTypeSerializer();
        String serialization = serializer.visit(multipleTrace);
        DataType multipleTrace_2 = DataTypeFactory.deserialize(serialization);

        assertEquals(multipleTrace, multipleTrace_2);
    }

    @Test
    void alignmentTest() throws IOException {
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment expected = new Alignment(logTrace, modelTrace);

        String JXESPathString = "src/test/resources/alignment/alignment.json";
        String JXESContents = Files.readString(Paths.get(JXESPathString));
        String serialization = Alignment.class.getName() + ":" + JXESContents;
        DataType output = DataTypeFactory.deserialize(serialization);
        assertEquals(expected, output);

    }

    @Test
    void alignmentInverseTest() {
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment alignment = new Alignment(logTrace, modelTrace);

        DataTypeSerializer serializer = new DataTypeSerializer();
        String serialization = serializer.visit(alignment);
        DataType alignment_2 = DataTypeFactory.deserialize(serialization);
        assertEquals(alignment, alignment_2);
    }

    @Test
    void alignmentTraceSwapInverseTest() throws IOException {
        // Serialization and deserialization is currently dependent on ordering of traces in a JXES string. Therefore,
        // they are only valid if alignments are given in the correct order
        Event el1 = new Event("C1", "A1", "1", new HashSet<>());
        Event el2 = new Event("C1", "A2", "2", new HashSet<>());
        Trace logTrace = new Trace(new ArrayList<>(List.of(el1, el2)));
        Event em1 = new Event("00", "A1", "00", new HashSet<>());
        Event em2 = new Event("00", "A3", "00", new HashSet<>());
        Trace modelTrace = new Trace(new ArrayList<>(List.of(em1, em2)));
        Alignment alignment_1 = new Alignment(logTrace, modelTrace);
        Alignment alignment_2 = new Alignment(modelTrace, logTrace);

        DataTypeSerializer serializer = new DataTypeSerializer();
        String output_1 = serializer.visit(alignment_1);
        String output_2 = serializer.visit(alignment_2);
        assertNotEquals(DataTypeFactory.deserialize(output_1),
                        DataTypeFactory.deserialize(output_2));
    }

    @Test
    void dataMapInverseTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("string", "str");
        map.put("int", 5);
        map.put("boolean", true);
        map.put("double", 5.0);
        map.put("list", new ArrayList<>(List.of('a', 'b', 'c')));

        Map<String, Integer> innerMap = new HashMap<>();
        innerMap.put("first", 1);
        innerMap.put("second", 2);
        map.put("HashMap", innerMap);

        Attribute<Double> attr = new Attribute<>("attrDoubleKey", 10.0);
        map.put("event", new Event("c1", "a1", "t1", new HashSet<>(List.of(attr))));
        DataMap dataMap = new DataMap(map);

        DataTypeSerializer serializer = new DataTypeSerializer();
        String serialization = serializer.visit(dataMap);
        DataType dataMap_2 = DataTypeFactory.deserialize(serialization);
        assertEquals(dataMap, dataMap_2);


    }



    public PetriNet getPetriNetExample() {
        /*
         *             --> p2 -
         *            /        \
         *   p1 --> t1          --> t2 --> p4
         *            \        /
         *             --> p3 -
         * */
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
        return new PetriNet(places, transitions, flowRelation);
    }

}