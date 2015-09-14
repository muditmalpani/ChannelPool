package com.w2a.channelPool;

public interface ChannelPool {
    /**
     * Checks if there are unused tokens and returns one if it finds it.
     * Otherwise create a new one, store and return it.
     */
    public String getToken();

    /**
     * Marks token as unused
     */
    public void releaseToken();

    /**
     * Updates the last ping time for that token.
     */
    public void ping();
}
