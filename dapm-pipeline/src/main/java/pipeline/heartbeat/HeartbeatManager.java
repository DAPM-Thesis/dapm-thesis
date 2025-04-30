package pipeline.heartbeat;

import communication.Consumer;
import communication.Producer;
import communication.Publisher;
import communication.Subscriber;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;
import communication.message.impl.Heartbeat;
import communication.message.impl.HeartbeatID;
import utils.TimeFormatter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Sends a heartbeat every <code>period</code> and records the heartbeats it
 * receives from its neighbours.
 */
public final class HeartbeatManager
        implements Publisher<Heartbeat>, Subscriber<Message>, AutoCloseable {
    private final HeartbeatID              myId     = new HeartbeatID();
    private final Map<HeartbeatID,Instant> lastSeen = new ConcurrentHashMap<>();

    private final VerificationStrategy     strategy;
    private final Duration                 period;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private volatile Producer  producer;           
    private volatile Consumer  consumer;           
    private volatile boolean   running = false;

    public HeartbeatManager(Duration       period,
                            VerificationStrategy strategy,
                            ProducerConfig  prodCfg,          
                            ConsumerConfig  consCfg,          
                            Set<HeartbeatID> expectedNeighbours)
    {
        this.period   = period;
        this.strategy = strategy;

        expectedNeighbours.forEach(id -> lastSeen.put(id, Instant.EPOCH));

        if (prodCfg != null)  this.producer = new Producer(prodCfg);
        if (consCfg != null)  this.consumer = new Consumer(this, consCfg);
        safeStartConsumer();
        System.out.println("[HB-INIT] manager created for id=");
    }

    @Override
    public void publish(Heartbeat hb) {
        if (producer != null) {
            producer.publish(hb);
            System.out.printf("[HB-SEND ] %s  id=%s  topic=%s%n",
                  TimeFormatter.ts(hb.getTime()), myId, producerTopic(producer));
        }
            
    }

    private static String producerTopic(Producer p){
        try{
            var f = p.getClass().getDeclaredField("topic");
            f.setAccessible(true);
            return (String) f.get(p);
        }catch(Exception e){
            return "?";
        }
    }

    @Override
    public void registerProducer(ProducerConfig cfg) {
        if (cfg == null) return;
        this.producer = new Producer(cfg);
    }

    public void registerConsumer(ConsumerConfig cfg) {
        if (cfg == null) return;
        this.consumer = new Consumer(this, cfg);
        safeStartConsumer();
    }

    private void safeStartConsumer() {
        if (consumer != null) {
            // calling start() multiple times on the same Consumer is harmless
            consumer.start();
        }
    }

    @Override
    public void observe(Message m, int port) {
        if (m instanceof Heartbeat hb) {
            lastSeen.put(hb.getId(), hb.getTime());
            System.out.printf("[HB-RECV ] %s  from=%s  → my=%s  port=%d%n", TimeFormatter.ts(hb.getTime()), hb.getId(), myId, port);
        }
    }

    public synchronized void start() {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(
                () -> publish(new Heartbeat(myId, Instant.now())),
                0, period.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean isAlive() {
        //return strategy.verify(lastSeen, Instant.now());
        boolean ok = strategy.verify(lastSeen, Instant.now());
        System.out.printf("[HB-CHECK] %s  %s → %s%n",
                          TimeFormatter.ts(Instant.now()), myId,
                          ok ? "ALIVE" : "DEAD" );
        return ok;
    }

    // TODO: update the logic according to the shuttin down process from CY
    @Override
    public void close() {
        scheduler.shutdownNow();
    }

    public HeartbeatID id() { return myId; }
}
