package communication.channel;

public class SimpleChannelFactory implements ChannelFactory {
    @Override
    public Channel createChannel() {
        return new Channel();
    }
}
