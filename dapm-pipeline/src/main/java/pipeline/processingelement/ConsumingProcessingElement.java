package pipeline.processingelement;

import communication.message.Message;

import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement {
    /** Holds the input types and their multiplicities. So if processing element consumes Event's from two
     *  separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     *  (PetriNet.class, 1).*/
    protected final Map<Class<? extends Message>, Integer> inputs;

    protected ConsumingProcessingElement() {
        this.inputs = setConsumedInputs();
    }

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs();
}
