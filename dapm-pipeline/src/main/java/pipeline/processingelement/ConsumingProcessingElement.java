package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;
import communication.config.ConsumerConfig;
import communication.message.Message;
import exceptions.PipelineExecutionException;
import utils.LogUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement implements Subscriber<Message> {
    /**
     * Holds the input types and their multiplicities. So if processing element consumes Event's from two
     * separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     * (PetriNet.class, 1).
     */
    protected final Map<Class<? extends Message>, Integer> inputs;
    private Map<Integer, Consumer> consumers = new HashMap<>();

    protected ConsumingProcessingElement() {
        this.inputs = setConsumedInputs();
        for (int typeCount : inputs.values()) {
            assert typeCount > 0 : "every provided input type must occur a positive number of times.";
        }
    }

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs();

    @Override
    public void start() {
        for (Consumer consumer : consumers.values()) {
            try {
                consumer.start();
            } catch (Exception e) {
                throw new PipelineExecutionException("Failed to start a consumer.", e);
            }
        }
    }

    @Override
    public void stop() {
        Exception firstException = null;
        for (Consumer consumer : consumers.values()) {
            try {
                consumer.stop();
            } catch (Exception e) {
                if (firstException == null) {
                    firstException = e;
                }
                LogUtil.error(e, "Failed to stop a consumer.");
            }
        }
        if (firstException != null) {
            throw new PipelineExecutionException("Failed to stop one or more consumers.", firstException);
        }
    }

    @Override
    public void terminate() {
        Exception firstException = null;
        for (Consumer consumer : consumers.values()) {
            try {
                consumer.terminate();
            } catch (Exception e) {
                if (firstException == null) {
                    firstException = e;
                }
                LogUtil.error(e, "Failed to terminate a consumer.");
            }
        }
        consumers.clear();
        if (firstException != null) {
            throw new PipelineExecutionException("Failed to terminate one or more consumers.", firstException);
        }
    }

    @Override
    public void registerConsumer(ConsumerConfig config) {
        if (!consumers.containsKey(config.portNumber())) {
            Consumer consumer = new Consumer(this, config);
            consumers.put(config.portNumber(), consumer);
        } else {
            LogUtil.debug("Consumer already registered with port number {}.", config.portNumber());
        }
    }
}
