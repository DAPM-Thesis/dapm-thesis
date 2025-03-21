package impl;

import communication.channel.Channel;
import communication.channel.ChannelFactory;

public class SimpleChannelFactory implements ChannelFactory {
    @Override
    public <T> Channel<T> createChannel() {
        return new Channel<>();
    }
}
