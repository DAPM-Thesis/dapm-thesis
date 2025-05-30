package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import utils.LogUtil;

public abstract class WebSource<O extends Message> extends Source<O> {
    private volatile Disposable subscription;

    public WebSource(Configuration configuration) { super(configuration); }

    // @Override
    // public boolean start() {
    //     try {
    //         subscription = process().subscribe(this::publish);
    //         finalizeStartupAndStartHeartbeat_Phase1();
    //         return !subscription.isDisposed();
    //     } catch (Exception e) {
    //         throw new RuntimeException("Exception in WebSource", e);
    //     }
    // }

    // protected abstract Flux<O> process();

    // @Override
    // public boolean terminate() {
    //     assert subscription != null;
    //     subscription.dispose();
    //     subscription = null;
    //     return super.terminate();
    // }

    @Override
    public boolean start() {
        LogUtil.info("[WSRC] {} Instance {}: Starting WebSource...", this.getClass().getSimpleName(), getInstanceId());
        if (subscription != null && !subscription.isDisposed()) { /* ... already started ... */ return true; }
        try {
            setAvailable(true); // Mark available early
            Flux<O> dataFlux = process();
            if (dataFlux == null) { /* ... error log ... */ setAvailable(false); return false; }

            this.subscription = dataFlux
                .doOnSubscribe(s -> LogUtil.info("[WSRC] {} Instance {}: Subscribed to reactive stream.", getClass().getSimpleName(), getInstanceId()))
                .doOnError(e -> {
                    LogUtil.error(e, "[WSRC ERR] {} Instance {}: Error in reactive stream. PE may become unavailable.", getClass().getSimpleName(), getInstanceId());
                    setAvailable(false); // Critical error in source
                })
                .doOnComplete(() -> {
                    LogUtil.info("[WSRC] {} Instance {}: Reactive stream completed.", getClass().getSimpleName(), getInstanceId());
                    // setAvailable(false); // Or isRunning = false if source is finite
                })
                .subscribe(
                    data -> { if (isAvailable()) publish(data); else LogUtil.info("[WSRC DROP] {} Not available, dropping: {}", getInstanceId(), data); },
                    error -> LogUtil.error(error, "[WSRC SUB ERR] {} Instance {}: Unhandled error in Flux subscription.", getClass().getSimpleName(), getInstanceId())
                );

            finalizeStartupAndStartHeartbeat(); // Start heartbeats

            LogUtil.info("[WSRC] {} Instance {}: WebSource start sequence complete.", this.getClass().getSimpleName(), getInstanceId());
            return true;
        } catch (Exception e) { /* ... error handling ... */ setAvailable(false); return false; }
    }

    protected abstract Flux<O> process();

    @Override
    public boolean terminate() {
        LogUtil.info("[WSRC] {} Instance {}: Terminating WebSource...", this.getClass().getSimpleName(), getInstanceId());
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        subscription = null;
        return super.terminate(); // Stops heartbeats and data producer
    }
}
