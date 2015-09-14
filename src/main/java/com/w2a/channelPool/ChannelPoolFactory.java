package com.w2a.channelPool;

public class ChannelPoolFactory {
    public static ChannelPool createChannelPool() {
        return new DatastoreChannelPool();
    }
}
