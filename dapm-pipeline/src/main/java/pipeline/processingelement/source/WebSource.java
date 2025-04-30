package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.accesscontrolled.PEToken;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public abstract class WebSource<O extends Message> extends Source<O> {
    protected WebSource(PEToken initialToken) {
        super(initialToken);
    }

    private Disposable subscription;

    @Override
    public void start() {
        subscription = process().subscribe(this::publish);
    }

    public abstract Flux<O> process();
}
