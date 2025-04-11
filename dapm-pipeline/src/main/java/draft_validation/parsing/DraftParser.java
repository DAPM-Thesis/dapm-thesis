package draft_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.JSONParsing;
import draft_validation.MetadataChannel;
import draft_validation.MetadataProcessingElement;
import utils.Pair;

import java.util.*;

public class DraftParser implements Parser<Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>>> {

    @Override
    public Pair<Collection<MetadataProcessingElement>, Collection<MetadataChannel>> deserialize(String str) {
        Map<String, Object> jsonMap = JSONParsing.toJSONMap(str);
        assert jsonMap.containsKey("\"processing elements\"") && jsonMap.containsKey("\"channels\"");

        // extract processing elements
        List<Map<String, Object>> elements = (List<Map<String, Object>>) jsonMap.get("\"processing elements\"");
        assert !elements.isEmpty();
        List<MetadataProcessingElement> processingElements = new ArrayList<>();
        for (Map<String, Object> elementMap : elements) {
            processingElements.add(getMetaDataProcessingElement(elementMap));
        }

        // extract channels
        List<List<Map<String, Object>>> rawChannels = (List<List<Map<String, Object>>>) jsonMap.get("\"channels\"");
        assert !rawChannels.isEmpty();
        List<MetadataChannel> channels = new ArrayList<>();
        for (List<Map<String, Object>> channel : rawChannels) {
            assert channel.size() == 2;
            MetadataProcessingElement from = getMetaDataProcessingElement(channel.get(0));
            MetadataProcessingElement to = getMetaDataProcessingElement(channel.get(1));
            channels.add(new MetadataChannel(from, to));
        }

        return new Pair<>(processingElements, channels);


    }

    private MetadataProcessingElement getMetaDataProcessingElement(Map<String, Object> elementMap) {
        assert elementMap.containsKey("\"orgID\"") && elementMap.containsKey("\"templateID\"") && elementMap.containsKey("\"inputs\"") && elementMap.containsKey("\"outputs\"");
        String orgID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"orgID\""));
        String templateID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"templateID\""));

        List<Class<? extends Message>> inputs = parseClassList((List<String>) elementMap.get("\"inputs\""));
        List<Class<? extends Message>> outputs = parseClassList((List<String>) elementMap.get("\"outputs\""));

        assert orgID != null && templateID != null;
        return new MetadataProcessingElement(orgID, templateID, inputs, outputs);
    }

    private List<Class<? extends Message>> parseClassList(List<String> messageClassList) {
        List<Class<? extends Message>> messageClasses = new ArrayList<>();
        for (String classString : messageClassList) {
            classString = JSONParsing.maybeRemoveOuterQuotes(classString);
            messageClasses.add(MessageTypeRegistry.getMessageType(classString));
        }

        return messageClasses;
    }
}
