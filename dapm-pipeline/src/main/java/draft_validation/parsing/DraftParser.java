package draft_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.JSONParsing;
import draft_validation.MetadataChannel;
import draft_validation.MetadataConsumer;
import draft_validation.MetadataProcessingElement;
import draft_validation.PipelineDraft;
import utils.Pair;

import java.util.*;

public class DraftParser implements Parser<PipelineDraft> {

    @Override
    public PipelineDraft deserialize(String str) {
        Map<String, Object> jsonMap = JSONParsing.toJSONMap(str);
        assert jsonMap.containsKey("\"processing elements\"") && jsonMap.containsKey("\"channels\"");

        // extract processing elements
        List<Map<String, Object>> elements = (List<Map<String, Object>>) jsonMap.get("\"processing elements\"");
        assert !elements.isEmpty();
        Set<MetadataProcessingElement> processingElements = new HashSet<>();
        for (Map<String, Object> elementMap : elements) {
            processingElements.add(getMetaDataProcessingElement(elementMap));
        }

        // extract channels
        List<List<Object>> rawChannels = (List<List<Object>>) jsonMap.get("\"channels\"");
        assert !rawChannels.isEmpty();
        Set<MetadataChannel> channels = new HashSet<>();
        for (List<Object> producerAndConsumers : rawChannels) {
            assert producerAndConsumers.size() == 2 : "There should be 1 producer and 1 list of consumers";
            MetadataProcessingElement producer = getMetaDataProcessingElement((Map<String, Object>) producerAndConsumers.getFirst());
            Set<MetadataConsumer> consumers = extractConsumers((List<List<Object>>) producerAndConsumers.get(1));
            channels.add(new MetadataChannel(producer, consumers));
        }

        return new PipelineDraft(processingElements, channels);
    }

    private Set<MetadataConsumer> extractConsumers(List<List<Object>> rawConsumers) {
        Set<MetadataConsumer> consumers = new HashSet<>();
        for (List<Object> rawConsumer : rawConsumers) {
            assert rawConsumer.size() == 2 : "Consumer should only consist of 1 processing element and 1 port";
            MetadataProcessingElement element = getMetaDataProcessingElement((Map<String, Object>) rawConsumer.getFirst());
            int port = Integer.parseInt((String) rawConsumer.get(1));
            consumers.add(new MetadataConsumer(element, port));
        }

        return consumers;
    }

    private MetadataProcessingElement getMetaDataProcessingElement(Map<String, Object> elementMap) {
        List<String> attributeNames = List.of("\"orgID\"", "\"templateID\"", "\"inputs\"", "\"output\"", "\"instanceID\"");
        assert attributeNames.stream().allMatch(elementMap::containsKey) : "All attributes must be present in the processing element";

        String orgID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"orgID\""));
        String templateID = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"templateID\""));

        List<Class<? extends Message>> inputs = parseClassList((List<String>) elementMap.get("\"inputs\""));
        String outputClassString = JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"output\""));
        Class<? extends Message> output = MessageTypeRegistry.getMessageType(outputClassString);

        int instanceID = Integer.parseInt((String) elementMap.get("\"instanceID\""));

        assert orgID != null && templateID != null;
        return new MetadataProcessingElement(orgID, templateID, inputs, output, instanceID);
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
