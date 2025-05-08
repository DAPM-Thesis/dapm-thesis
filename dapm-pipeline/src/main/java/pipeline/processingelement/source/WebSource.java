package pipeline.processingelement.source;

import communication.message.Message;
import pipeline.processingelement.Configuration;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import utils.LogUtil;

public abstract class WebSource<O extends Message> extends Source<O> {
    private Disposable subscription;

    public WebSource(Configuration configuration) { super(configuration); }

    @Override
    public boolean start() {
        try {
            subscription = process().subscribe(this::publish);
            return true;
        } catch (Exception e) {
            LogUtil.error(e, "Failed to start source.");
            return false;
        }
    }

    protected abstract Flux<O> process();

    @Override
    public boolean pause() {
        try {
            subscription.dispose();
            return super.pause();
        } catch (Exception e) {
            LogUtil.error(e, "Failed to pause source.");
            return false;
        }
    }
}
