package pipeline.processingelement;

import communication.Consumer;
import communication.ProducingProcessingElement;
import communication.Subscriber;
import communication.message.Message;
import utils.LogUtil;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement implements Subscriber<Pair<Message, Integer>> {
    /** Holds the input types and their multiplicities. So if processing element consumes Event's from two
     *  separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     *  (PetriNet.class, 1).*/
    protected final Map<Class<? extends Message>, Integer> inputs;
    private final Map<Integer, Consumer> consumers = new HashMap<>();

    public ConsumingProcessingElement(Configuration configuration) {
        super(configuration);
        this.inputs = setConsumedInputs();
        for (int typeCount : inputs.values()) {
            assert typeCount > 0 : "every provided input type must occur a positive number of times.";
        }
    }

    protected Map<Integer, Consumer> getConsumers() { return Map.copyOf(consumers);}

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs(); // TODO: refactor with annotations

    @Override
    public boolean start() {
        LogUtil.info("[CPE] {} Instance {}: Starting data consumers...", this.getClass().getSimpleName(), getInstanceId());
        boolean allConsumersStarted = true;
        if (consumers.isEmpty() && !(this instanceof ProducingProcessingElement) && (inputs != null && !inputs.isEmpty())) {
            LogUtil.info("[CPE WARN] {} Instance {}: No Kafka data consumers registered despite defined inputs. This might be a configuration error.", this.getClass().getSimpleName(), getInstanceId());
        } else if (consumers.isEmpty()) {
            LogUtil.info("[CPE] {} Instance {}: No Kafka data consumers to start.", this.getClass().getSimpleName(), getInstanceId());
        }

        for (Map.Entry<Integer, Consumer> entry : consumers.entrySet()) {
            Consumer consumer = entry.getValue();
            if (!consumer.start()) {
                LogUtil.info("[CPE ERR] {} Instance {}: Failed to start data consumer for port {} on topic {}.",
                             this.getClass().getSimpleName(), getInstanceId(), entry.getKey(), consumer.getTopic());
                allConsumersStarted = false;
                break; 
            } else {
                LogUtil.info("[CPE] {} Instance {}: Started data consumer for port {} on topic {}.",
                             this.getClass().getSimpleName(), getInstanceId(), entry.getKey(), consumer.getTopic());
            }
        }

        if (!allConsumersStarted) {
            LogUtil.info("[CPE ERR] {} Instance {}: Not all data consumers started. CPE start failed.", this.getClass().getSimpleName(), getInstanceId());
            setAvailable(false);
            consumers.values().forEach(c -> { c.stop(); c.terminate(); });
            return false;
        }
        setAvailable(true);
        LogUtil.info("[CPE] {} Instance {}: Data consumers started. Heartbeat to be handled by concrete type.", this.getClass().getSimpleName(), getInstanceId());
        return true;
    }

    @Override
    public boolean terminate() {
        LogUtil.info("[CPE] {} Instance {}: Terminating ConsumingProcessingElement's data consumers...", this.getClass().getSimpleName(), getInstanceId());
        boolean allDataConsumersStopped = true;
        for (Consumer consumer : consumers.values()) {
            if (!consumer.stop()) allDataConsumersStopped = false;
        }
        boolean allDataConsumersTerminated = true;
        for (Consumer consumer : consumers.values()) {
            if (!consumer.terminate()) allDataConsumersTerminated = false;
        }
        if (allDataConsumersTerminated) consumers.clear();
        
        boolean superTerminated = super.terminate();
        return allDataConsumersStopped && allDataConsumersTerminated && superTerminated;
    }

    public boolean stopDataConsumers() {
        LogUtil.info("[CPE] {} Instance {}: Stopping data consumers...", getClass().getSimpleName(), getInstanceId());
        boolean allStopped = true;
        for (Consumer consumer : consumers.values()) {
            if (!consumer.stop()) allStopped = false;
        }
        return allStopped;
    }

    public boolean resumeDataConsumers() {
        LogUtil.info("[CPE] {} Instance {}: Resuming data consumers...", getClass().getSimpleName(), getInstanceId());
        boolean allResumed = true;
        for (Consumer consumer : consumers.values()) {
            if (!consumer.start()) allResumed = false; 
        }
        return allResumed;
    }

    @Override
    public abstract void observe(Pair<Message, Integer> inputAndPortNumber);

    public final void registerConsumer(Consumer consumer, int portNumber) {
        consumers.put(portNumber, consumer);
    }
}
