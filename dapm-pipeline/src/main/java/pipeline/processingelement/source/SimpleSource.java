package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;
import utils.LogUtil;

import java.util.concurrent.ExecutorService; // Changed from ThreadPoolExecutor
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
// TODO: understand simple source and maybe distinguish instead by making a GeneratingSource (a source from which data is generated in the source).
public abstract class SimpleSource<O extends Message> extends Source<O> {

    private volatile boolean isRunning ;
    private ExecutorService executor;
    private Future<?> processingTask;

    public SimpleSource(Configuration configuration) { super(configuration); }

    // @Override
    // public boolean start() {
    //     try {
    //         isRunning = true;
    //         if(executor == null || executor.isShutdown()) {
    //             executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    //         }
    //         executor.submit(() -> {
    //             while(isRunning) {
    //                 O output = process();
    //                 publish(output);
    //             }
    //         });

    //         if(isRunning) finalizeStartupAndStartHeartbeat_Phase1();

    //         return isRunning;
    //     } catch (Exception e) {
    //         throw new RuntimeException("Exception in SimpleSource", e);
    //     }
    // }

    @Override
    public boolean start() {
        LogUtil.info("[SSRC] {} Instance {}: Starting SimpleSource...", this.getClass().getSimpleName(), getInstanceId());
        if (isRunning) { // Check specific flag
            LogUtil.info("[SSRC] {} Instance {}: Already running.", this.getClass().getSimpleName(), getInstanceId());
            return true;
        }
        try {
            isRunning = true;
            setAvailable(true); 
            
            if (executor == null || executor.isShutdown()) {
                executor = Executors.newSingleThreadExecutor();
            }
            processingTask = executor.submit(() -> {
                while (isRunning && !Thread.currentThread().isInterrupted() && isAvailable()) {
                    try {
                        O output = process(); 
                        if (output != null && isRunning && isAvailable()) {
                            publish(output);
                        }
                    } catch (Exception e) {
                        LogUtil.error(e, "[SSRC LOOP ERR] {} Instance {}: Error in processing loop. Loop continues.", this.getClass().getSimpleName(), getInstanceId());
                    }
                }
                isRunning = false; 
            });

            // Start heartbeats after main logic is initiated
            finalizeStartupAndStartHeartbeat(); 

            LogUtil.info("[SSRC] {} Instance {}: SimpleSource start sequence complete.", this.getClass().getSimpleName(), getInstanceId());
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "[SSRC ERR] {} Instance {}: Failed to start SimpleSource", this.getClass().getSimpleName(), getInstanceId());
            isRunning = false;
            setAvailable(false);
            if (executor != null) executor.shutdownNow();
            return false;
        }
    }

    protected abstract O process();

    // @Override
    // public boolean terminate() {
    //     isRunning = false;
    //     assert executor != null;
    //     executor.shutdown();
    //     executor = null;
    //     return super.terminate();
    // }

    @Override
    public boolean terminate() {
        LogUtil.info("[SSRC] {} Instance {}: Terminating SimpleSource...", this.getClass().getSimpleName(), getInstanceId());
        isRunning = false;
        if (processingTask != null && !processingTask.isDone()) {
            processingTask.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
            } catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
            executor = null;
        }
        return super.terminate(); // Stops heartbeats and data producer
    }

}
