package draft_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.parsing.JSONParser;
import draft_validation.MetadataChannel;
import draft_validation.MetadataConsumer;
import draft_validation.MetadataProcessingElement;
import draft_validation.PipelineDraft;

import java.util.*;

public class DraftParser implements Parser<PipelineDraft> {

    @Override
    public PipelineDraft deserialize(String str) throws InvalidDraft {
        Map<String, Object> jsonMap = (Map<String, Object>) (new JSONParser()).parse(str);
        if (!(jsonMap.containsKey("processing elements") && jsonMap.containsKey("channels")))
            { throw new InvalidDraft("\"processing elements\" and/or \"channels\" keys missing from draft"); }

        // extract processing elements
        List<Map<String, Object>> elements = (List<Map<String, Object>>) jsonMap.get("processing elements");
        if (elements.size() < 2)
            { throw new InvalidDraft("A minimal pipeline contains 2 elements: a source and a sink. " + elements.size() + " provided in draft."); }

        Set<MetadataProcessingElement> processingElements = new HashSet<>();
        for (Map<String, Object> elementMap : elements) {
            processingElements.add(getMetaDataProcessingElement(elementMap));
        }

        // extract channels
        List<List<Object>> rawChannels = (List<List<Object>>) jsonMap.get("channels");

        if (rawChannels.isEmpty())
            { throw new InvalidDraft("A minimal pipeline contains 1 channel (between a source and a sink). No channels founds."); }

        Set<MetadataChannel> channels = new HashSet<>();
        for (List<Object> producerAndConsumers : rawChannels) {
            if (producerAndConsumers.size() != 2)
                { throw new InvalidDraft("Invalid channel found. It should contain 1 producer and 1 list of consumers. Provided: " + producerAndConsumers); }

            MetadataProcessingElement producer = getMetaDataProcessingElement((Map<String, Object>) producerAndConsumers.getFirst());
            Set<MetadataConsumer> consumers = extractConsumers((List<List<Object>>) producerAndConsumers.get(1));
            channels.add(new MetadataChannel(producer, consumers));
        }

        return new PipelineDraft(processingElements, channels);
    }

    private Set<MetadataConsumer> extractConsumers(List<List<Object>> rawConsumers) throws InvalidDraft {
        Set<MetadataConsumer> consumers = new HashSet<>();
        for (List<Object> rawConsumer : rawConsumers) {
            if (rawConsumer.size() != 2)
                { throw new InvalidDraft("Consumer should consist of exactly 1 processing element and 1 port, but given: " + rawConsumer); }

            MetadataProcessingElement element = getMetaDataProcessingElement((Map<String, Object>) rawConsumer.getFirst());

            int port = (int) rawConsumer.get(1);
            consumers.add(new MetadataConsumer(element, port));
        }

        return consumers;
    }

    private MetadataProcessingElement getMetaDataProcessingElement(Map<String, Object> elementMap) throws InvalidDraft {
        List<String> attributeNames = List.of("orgID", "templateID", "inputs", "output", "instanceID");
        if (!(attributeNames.stream().allMatch(elementMap::containsKey)))
            { throw new InvalidDraft("All attributes must be present in the processing element."); }

        String orgID = extractOrgID(elementMap);
        String templateID = extractTemplateID(elementMap);
        List<Class<? extends Message>> inputs = parseClassList((List<String>) elementMap.get("inputs"));
        Class<? extends Message> output = extractOutput(elementMap);
        int instanceID = extractInstanceID(elementMap);

        return new MetadataProcessingElement(orgID, templateID, inputs, output, instanceID);
    }

    private String extractTemplateID(Map<String, Object> elementMap) throws InvalidDraft {
        String templateID = (String) elementMap.get("templateID"); //TODO: remove JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"templateID\""));
        if (templateID.isEmpty())
            { throw new InvalidDraft("templateID must be present and non-empty."); }
        return templateID;
    }

    private String extractOrgID(Map<String, Object> elementMap) throws InvalidDraft {
        String orgID = (String) elementMap.get("orgID"); // TODO: remove JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"orgID\""));
        if (orgID.isEmpty())
            { throw new InvalidDraft("orgID must be present and non-empty in the processing element."); }
        return orgID;
    }

    private int extractInstanceID(Map<String, Object> elementMap) throws InvalidDraft {
        return (int) elementMap.get("instanceID");
        // TODO: remove
        /*
        if ((int) elementMap.get("instanceID") <= 0) {
            throw new InvalidDraft("Invalid instance ID (must be a positive integer by convention): " + elementMap.get("instanceID"));
        }
        try { (int) elementMap.get("instanceID") = elementMap.get("instanceID"); }
        catch (NumberFormatException e) { throw new InvalidDraft("Invalid instance ID: " + elementMap.get("\"instanceID\"")); }
        if ((int) elementMap.get("instanceID") <= 0)
            { throw new InvalidDraft("Invalid instance ID (must be a positive integer by convention): " + elementMap.get("\"instanceID\"")); }
        return (int) elementMap.get("instanceID");

         */
    }

    private Class<? extends Message> extractOutput(Map<String, Object> elementMap) throws InvalidDraft {
        String outputClassString = (String) elementMap.get("output"); // TODO: remove JSONParsing.maybeRemoveOuterQuotes((String) elementMap.get("\"output\""));
        if (outputClassString == null)
            { return null; }

        if (MessageTypeRegistry.getMessageType(outputClassString) == null)
            { throw new InvalidDraft("Invalid output class: " + outputClassString); }
        return MessageTypeRegistry.getMessageType(outputClassString);
    }

    private List<Class<? extends Message>> parseClassList(List<String> messageClassList) throws InvalidDraft {
        List<Class<? extends Message>> messageClasses = new ArrayList<>();
        for (String classString : messageClassList) {
            // TODO: remove: classString = JSONParsing.maybeRemoveOuterQuotes(classString);
            if (MessageTypeRegistry.getMessageType(classString) == null)
                { throw new  InvalidDraft("Invalid message class: " + classString); }
            messageClasses.add(MessageTypeRegistry.getMessageType(classString));
        }

        return messageClasses;
    }
}
