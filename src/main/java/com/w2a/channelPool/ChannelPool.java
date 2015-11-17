package com.w2a.channelPool;

public interface ChannelPool {
    /**
     * Checks if there are unused tokens and returns one if it finds it.
     * Otherwise create a new one, store and return it.
     */
    public String getToken(String clientId);

    /**
     * Marks token as unused
     */
    public void releaseToken(String token);

    /**
     * Marks the tokens which haven't been pinged for `timeoutInMillis` ms as
     * unused
     */
    public void releaseUnusedTokens(long timeoutInMillis);

    /**
     * Updates the last ping time for that token.
     */
    public void ping(String token);

    /**
     * Removes all existing tokens from the pool
     */
    public void reset();
}
