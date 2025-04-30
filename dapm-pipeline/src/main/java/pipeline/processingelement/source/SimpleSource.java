package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.accesscontrolled.PEToken;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class SimpleSource<O extends Message> extends Source<O> {

    protected SimpleSource(PEToken initialToken) {
        super(initialToken);
    }

    public volatile boolean isRunning = true;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    @Override
    public void start() {
        executor.submit(() -> {
            while(isRunning) {
                O output = process();
                publish(output);
            }
        });
    }

    public abstract O process();
}
