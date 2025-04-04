package draft_validation.parsing;

import communication.message.Message;
import communication.message.serialization.JSONParsing;
import communication.message.serialization.JXESParsing;
import draft_validation.MetadataChannel;
import draft_validation.MetadataProcessingElement;
import utils.Pair;

import java.util.*;

public class DraftParser implements Parser<Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>>> {

    @Override
    public Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> deserialize(String str) {
        return new Pair<>(new ArrayList<>(), new ArrayList<>());
        /*
        Map<String, Object> jsonMap = JSONParsing.toJSONMap(str);
        assert jsonMap.containsKey("processing elements") && jsonMap.containsKey("channels");
        List<Map<String, Object>> elements = (List<Map<String, Object>>) jsonMap.get("processing elements");
        System.out.println("\n\ndraftparser 21\n\n");
        assert !elements.isEmpty();
        // extract processing elements
        for (Map<String, Object> elementMap : elements) {
            assert elementMap.containsKey("\"orgID\"") && elementMap.containsKey("\"templateID\"") && elementMap.containsKey("\"inputs\"") && elementMap.containsKey("\"outputs\"");
            String orgID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"orgID\""));
            String templateID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"templateID\""));

            List<Class<? extends Message>> inputs = parseList((List<String>) elementMap.get("\"inputs\""));
            List<Class<? extends Message>> outputs = parseList((List<String>) elementMap.get("\"outputs\""));

            assert orgID != null && templateID != null && inputs != null && outputs != null;

        }

        // extract channels
        return new Pair<>(new ArrayList<>(), new ArrayList<>());

         */
    }

    private List<Class<? extends Message>> parseList(List<String> messageClassList) {

        List<Class<? extends Message>> messageClasses = new ArrayList<>();
        for (String messageClass : messageClassList) {
            messageClass = JSONParsing.maybeRemoveOuterQuotes(messageClass);
        }

        return new ArrayList<>();
    }
}
