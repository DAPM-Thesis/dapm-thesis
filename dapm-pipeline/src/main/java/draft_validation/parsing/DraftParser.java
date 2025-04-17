package draft_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.parsing.JSONParser;
import draft_validation.MetadataChannel;
import draft_validation.MetadataSubscriber;
import draft_validation.MetadataProcessingElement;
import draft_validation.PipelineDraft;
import java.util.*;

public class DraftParser implements Parser<PipelineDraft> {

    @Override
    public PipelineDraft deserialize(String json) {
        // The JSON schema validator below will take care of throwing errors if the json is not correctly formatted
        // according to the pipeline_draft_json_schema.json. We can therefore omit throwing those errors afterwards.
        JsonSchemaValidator.validatePipelineDraft(json);

        Map<String, Object> jsonMap = (Map<String, Object>) (new JSONParser()).parse(json);


        List<Map<String, Object>> rawElements = (List<Map<String, Object>>) jsonMap.get("processing elements");
        Set<MetadataProcessingElement> elements = getMetaDataProcessingElements(rawElements);

        List<Map<String, Object>> rawChannels = (List<Map<String, Object>>) jsonMap.get("channels");
        Set<MetadataChannel> channels = getMetaDataChannels(rawChannels);

        return new PipelineDraft(elements, channels);
    }

    private Set<MetadataChannel> getMetaDataChannels(List<Map<String, Object>> rawChannels) {
        Set<MetadataChannel> channels = new HashSet<>();
        for (Map<String, Object> rawChannel : rawChannels) {
            channels.add(getMetaDataChannel(rawChannel));
        }
        return channels;
    }

    private MetadataChannel getMetaDataChannel(Map<String, Object> rawChannel) {
        MetadataProcessingElement publisher = getMetaDataProcessingElement((Map<String, Object>) rawChannel.get("publisher"));
        List<Map<String, Object>> subscribersList = (List<Map<String, Object>>) rawChannel.get("subscribers");
        Set<MetadataSubscriber> subscribers = getMetaDataSubscribers(subscribersList);
        return new MetadataChannel(publisher, subscribers);
    }

    private Set<MetadataSubscriber> getMetaDataSubscribers(List<Map<String, Object>> subscribersList) {
        Set<MetadataSubscriber> subscribers = new HashSet<>();
        for (Map<String, Object> rawSubscriber : subscribersList) {
            subscribers.add(getMetaDataSubscriber(rawSubscriber));
        }
        return subscribers;
    }

    private MetadataSubscriber getMetaDataSubscriber(Map<String, Object> rawSubscriber) {
        MetadataProcessingElement element = getMetaDataProcessingElement((Map<String, Object>) rawSubscriber.get("processing element"));
        int portNumber = (int) rawSubscriber.get("portNumber");
        return new MetadataSubscriber(element, portNumber);
    }

    private Set<MetadataProcessingElement> getMetaDataProcessingElements(List<Map<String, Object>> rawElements) {
        Set<MetadataProcessingElement> elements = new HashSet<>();
        for (Map<String, Object> elementMap : rawElements) {
            elements.add(getMetaDataProcessingElement(elementMap));
        }
        return elements;
    }


    private MetadataProcessingElement getMetaDataProcessingElement(Map<String, Object> elementMap) throws InvalidDraft {
        String orgID = (String) elementMap.get("organizationID");
        String templateID = (String) elementMap.get("templateID");
        List<Class<? extends Message>> inputs = parseClassList((List<String>) elementMap.get("inputs"));
        Class<? extends Message> output = extractOutput(elementMap);
        int instanceNumber = (int) elementMap.get("instanceNumber");

        return new MetadataProcessingElement(orgID, templateID, inputs, output, instanceNumber);
    }


    private Class<? extends Message> extractOutput(Map<String, Object> elementMap) throws InvalidDraft {
        String outputClassString = (String) elementMap.get("output");
        if (outputClassString == null)
            { return null; }
        return MessageTypeRegistry.getMessageType(outputClassString);
    }

    private List<Class<? extends Message>> parseClassList(List<String> messageClassList) throws InvalidDraft {
        List<Class<? extends Message>> messageClasses = new ArrayList<>();
        for (String classString : messageClassList) {
            messageClasses.add(MessageTypeRegistry.getMessageType(classString));
        }
        return messageClasses;
    }
}
