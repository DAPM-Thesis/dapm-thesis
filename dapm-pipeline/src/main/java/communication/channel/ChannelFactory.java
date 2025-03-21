package communication.channel;

public interface ChannelFactory {
    <C> Channel<C> createChannel();
}
