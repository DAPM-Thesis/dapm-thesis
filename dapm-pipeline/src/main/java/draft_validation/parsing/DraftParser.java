package draft_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.parsing.JSONParser;
import draft_validation.ChannelReference;
import draft_validation.SubscriberReference;
import draft_validation.ProcessingElementReference;
import draft_validation.PipelineDraft;
import java.util.*;

public class DraftParser implements Parser<PipelineDraft> {

    @Override
    public PipelineDraft deserialize(String json) {
        // The JSON schema validator below will take care of throwing errors if the json is not correctly formatted
        // according to the pipeline_draft_json_schema.json. We can therefore omit throwing those errors afterward.
        JsonSchemaValidator.validatePipelineDraft(json);

        Map<String, Object> jsonMap = (Map<String, Object>) (new JSONParser()).parse(json);


        List<Map<String, Object>> rawElements = (List<Map<String, Object>>) jsonMap.get("processing elements");
        Set<ProcessingElementReference> elements = getProcessingElementReferences(rawElements);

        List<Map<String, Object>> rawChannels = (List<Map<String, Object>>) jsonMap.get("channels");
        Set<ChannelReference> channels = getChannelReferences(rawChannels);

        return new PipelineDraft(elements, channels);
    }

    private Set<ChannelReference> getChannelReferences(List<Map<String, Object>> rawChannels) {
        Set<ChannelReference> channels = new HashSet<>();
        for (Map<String, Object> rawChannel : rawChannels) {
            channels.add(getChannelReference(rawChannel));
        }
        return channels;
    }

    private ChannelReference getChannelReference(Map<String, Object> rawChannel) {
        ProcessingElementReference publisher = getProcessingElementReferences((Map<String, Object>) rawChannel.get("publisher"));
        List<Map<String, Object>> subscribersList = (List<Map<String, Object>>) rawChannel.get("subscribers");
        Set<SubscriberReference> subscribers = getSubscriberReferences(subscribersList);
        return new ChannelReference(publisher, subscribers);
    }

    private Set<SubscriberReference> getSubscriberReferences(List<Map<String, Object>> subscribersList) {
        Set<SubscriberReference> subscribers = new HashSet<>();
        for (Map<String, Object> rawSubscriber : subscribersList) {
            subscribers.add(getSubscriberReference(rawSubscriber));
        }
        return subscribers;
    }

    private SubscriberReference getSubscriberReference(Map<String, Object> rawSubscriber) {
        ProcessingElementReference element = getProcessingElementReferences((Map<String, Object>) rawSubscriber.get("processing element"));
        int portNumber = (int) rawSubscriber.get("portNumber");
        return new SubscriberReference(element, portNumber);
    }

    private Set<ProcessingElementReference> getProcessingElementReferences(List<Map<String, Object>> rawElements) {
        Set<ProcessingElementReference> elements = new HashSet<>();
        for (Map<String, Object> elementMap : rawElements) {
            elements.add(getProcessingElementReferences(elementMap));
        }
        return elements;
    }


    private ProcessingElementReference getProcessingElementReferences(Map<String, Object> elementMap) throws InvalidDraft {
        String organizationID = (String) elementMap.get("organizationID");
        String organizationHostURL = (String) elementMap.get("hostURL");
        String templateID = (String) elementMap.get("templateID");
        List<Class<? extends Message>> inputs = parseClassList((List<String>) elementMap.get("inputs"));
        Class<? extends Message> output = extractOutput(elementMap);
        int instanceNumber = (int) elementMap.get("instanceNumber");

        return new ProcessingElementReference(
                organizationID, organizationHostURL, templateID,inputs, output, instanceNumber);
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
