package pipeline.processingelement.source;

import communication.message.Message;
import exceptions.PipelineExecutionException;
import org.apache.commons.logging.Log;
import utils.LogUtil;
import pipeline.processingelement.Configuration;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
// TODO: understand simple source and maybe distinguish instead by making a GeneratingSource (a source from which data is generated in the source).
public abstract class SimpleSource<O extends Message> extends Source<O> {

    private volatile boolean isRunning;
    private ThreadPoolExecutor executor;

    public SimpleSource(Configuration configuration) { super(configuration); }

    @Override
    public boolean start() {
        try {
            isRunning = true;
            if(executor == null || executor.isShutdown()) {
                executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
            }
            executor.submit(() -> {
                while(isRunning) {
                    O output = process();
                    publish(output);
                }
            });
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to start source.");
            isRunning = false;
            return false;
        }
    }

    protected abstract O process();

    @Override
    public boolean pause() {
        try {
            isRunning = false;
            executor.shutdown();
            LogUtil.debug("Source has been paused.");
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to pause source.");
            return false;
        }
    }

    @Override
    public boolean terminate() {
      return pause();
    }
}
