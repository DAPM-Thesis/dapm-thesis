package communication.channel;

public class SimpleChannelFactory implements ChannelFactory {
    @Override
    public <T> Channel<T> createChannel() {
        return new Channel<>();
    }
}
