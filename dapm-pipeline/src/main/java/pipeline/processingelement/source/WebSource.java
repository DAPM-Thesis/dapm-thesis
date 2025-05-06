package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Map;

public abstract class WebSource<O extends Message> extends Source<O> {
    private Disposable subscription;

    public WebSource(Configuration configuration) { super(configuration); }

    @Override
    public void start() {
        subscription = process().subscribe(this::publish);
    }

    protected abstract Flux<O> process();
}
