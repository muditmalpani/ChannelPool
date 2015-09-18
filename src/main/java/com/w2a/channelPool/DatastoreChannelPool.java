package com.w2a.channelPool;

public class DatastoreChannelPool implements ChannelPool {

    @Override
    public String getToken(String clientId) {
        return null;
    }

    @Override
    public void releaseToken(String token) {
    }

    @Override
    public void ping(String token) {
    }

    @Override
    public void reset() {
    }

}
