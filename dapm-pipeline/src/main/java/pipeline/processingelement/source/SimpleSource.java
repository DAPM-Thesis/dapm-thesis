package pipeline.processingelement.source;

import communication.message.Message;
import exceptions.PipelineExecutionException;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class SimpleSource<O extends Message> extends Source<O> {

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

    @Override
    public void stop() {
        try {
            executor.shutdown();
        } catch (Exception e) {
            throw new PipelineExecutionException("Failed to stop source.", e);
        }
    }

    public abstract O process();
}
