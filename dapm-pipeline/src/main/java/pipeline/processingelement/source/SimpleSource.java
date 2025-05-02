package pipeline.processingelement.source;

import communication.message.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class SimpleSource<O extends Message> extends Source<O> {

    private volatile boolean isRunning = true;
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

    protected abstract O process();
}
