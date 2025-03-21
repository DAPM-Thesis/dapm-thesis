package communication.channel;

public class SimpleChannelFactory implements ChannelFactory {
    @Override
    public <C> Channel<C> createChannel() {
        return new Channel<>();
    }
}
