package pipeline.processingelement.source;

import communication.message.Message;
import exceptions.PipelineExecutionException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public abstract class WebSource<O extends Message> extends Source<O> {
    private Disposable subscription;

    @Override
    public void start() {
        subscription = process().subscribe(this::publish);
    }

    protected abstract Flux<O> process();

    @Override
    public void stop() {
        try {
            subscription.dispose();
        } catch (Exception e) {
            throw new PipelineExecutionException("Failed to stop source.", e);
        }
    }
}
