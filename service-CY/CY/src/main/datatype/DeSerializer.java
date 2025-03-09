package main.datatype;

import main.datatype.petrinet.PetriNet;
import main.datatype.petrinet.Place;
import main.datatype.petrinet.Transition;
import main.datatype.petrinet.arc.Arc;
import main.datatype.petrinet.arc.PlaceToTransitionArc;
import main.datatype.petrinet.arc.TransitionToPlaceArc;
import main.utils.Pair;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeSerializer {

    /** Converts a PNML string to a PetriNet. */
    public static PetriNet PNMLToPetriNet(String pnml) {
        Set<Place> places = extractPlaces(pnml);
        Set<Transition> transitions = extractTransitions(pnml);
        Set<Arc> flowRelation = extractFlowRelation(pnml, places, transitions);
        return new PetriNet(places, transitions, flowRelation);
    }


    /** Extracts the places from a PNML string. If a place does not have an initialMarking set, its marking will be
     * set to 0. */
    private static Set<Place> extractPlaces(String pnml) {
        Set<Place> places = new HashSet<>();
        String start = "<place";
        String end = "</place>";
        String initialMarkingText = "<initialMarking>";
        int i = 0;
        int markingEnd;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; } // String.indexOf() returns -1 if no matches are found
            // extract ID
            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.getFirst();
            i = IDAndEnd.getSecond();

            // extract marking if there is one
            int initialMarkingIndex = pnml.indexOf(initialMarkingText, i);
            int endIndex = pnml.indexOf(end, i);
            assert endIndex != -1 : "'<place' is not closed by '</place>'";
            int marking = 0;
            // a marking is found within the current place's bounds
            if (initialMarkingIndex != -1 && initialMarkingIndex < endIndex) {
                String text = "<text>";
                int markingStart = pnml.indexOf(text, initialMarkingIndex+initialMarkingText.length());
                markingStart = markingStart + text.length();
                markingEnd = pnml.indexOf('<', markingStart+1);
                //substring inside <text> </text> with whitespace removed
                marking = Integer.parseInt(
                        pnml.substring(markingStart, markingEnd).replaceAll("\\s",""));
            }
            places.add(new Place(ID, marking));
            i = endIndex+1;
        }
        return places;
    }

    private static Set<Transition> extractTransitions(String pnml) {
        Set<Transition> transitions = new HashSet<>();
        String start = "<transition";
        int i = 0;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; }

            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.getFirst();
            i = IDAndEnd.getSecond();

            transitions.add(new Transition(ID));
        }
        return transitions;
    }

    private static Set<Arc> extractFlowRelation(String pnml, Set<Place> places, Set<Transition> transitions) {
        Set<Arc> flowRelation = new HashSet<>();
        // Making hashmaps serves two purposes: 1) it makes it easier to determine whether source/target of the pnml arc
        // is a place/transitions, and 2) it makes the Arc map to the provided place/transition objects (efficiently).
        Map<String, Place> placeMap = new HashMap<>();
        Map<String, Transition> transitionMap = new HashMap<>();
        for (Place place : places) { placeMap.put(place.getID(), place); }
        for (Transition transition : transitions) { transitionMap.put(transition.getID(), transition); }

        String start = "<arc";
        int i = 0;
        while (i < pnml.length()) {
            i = pnml.indexOf(start, i);
            if (i == -1) { break; }

            Pair<String, Integer> IDAndEnd = getNextQuoteContent(pnml, i+start.length());
            String ID = IDAndEnd.getFirst();
            Pair<String, Integer> sourceAndEnd = getNextQuoteContent(pnml, IDAndEnd.getSecond());
            String source = sourceAndEnd.getFirst();
            Pair<String, Integer> targetAndEnd = getNextQuoteContent(pnml, sourceAndEnd.getSecond());
            String target = targetAndEnd.getFirst();
            i = targetAndEnd.getSecond();

            if (placeMap.containsKey(source) && transitionMap.containsKey(target))
                { flowRelation.add(new PlaceToTransitionArc(ID, placeMap.get(source), transitionMap.get(target))); }
            else if (transitionMap.containsKey(source) && placeMap.containsKey(target))
                { flowRelation.add(new TransitionToPlaceArc(ID, transitionMap.get(source), placeMap.get(target)));
            } else
                { throw new IllegalStateException("Arc did not match a pair of Place & Transition in the PNML."); }

        }
        return flowRelation;
    }

    /** Fails if there is no pair of quotations in the given string starting from startIndex.
     * @return The next quote content [String] and the index right after the end quote's index, i.e. if i_e is the end
     * quote index, the returned integer is (i_e+1). */
    private static Pair<String, Integer> getNextQuoteContent(String str, int startIndex) {
        int startQuoteIndex = str.indexOf('\"', startIndex);
        int endQuoteIndex = str.indexOf('\"', startQuoteIndex+1);
        assert startQuoteIndex != -1 && endQuoteIndex != -1 && (startQuoteIndex+1) < endQuoteIndex
                : "must have \" pair and non-empty content";

        String content = str.substring(startQuoteIndex+1, endQuoteIndex);
        return new Pair<>(content, endQuoteIndex+1);
    }

    public Event JXESToEvent(String Event) {
        // TODO: implement
        return new Event("unimplemented", "unimplemented", "15-05", new HashSet<>());
    }

}
