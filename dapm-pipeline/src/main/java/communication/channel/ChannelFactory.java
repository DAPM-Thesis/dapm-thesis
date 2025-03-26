package communication.channel;

public interface ChannelFactory {
    <T> Channel<T> createChannel();
}
