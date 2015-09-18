package com.w2a.channelPool;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * In memory implementation of ChannelPool.
 * In this implementation, each VM will maintain its own pool and
 * the pool will be reset whenever the VM is shut down.
 * Please note that this implementation is not thread safe.
 */
public class InMemoryChannelPool implements ChannelPool {
    private Map<String, TokenInfo> activeTokenMap;
    private Queue<String> unusedTokenPool;

    public InMemoryChannelPool() {
        this.activeTokenMap = new HashMap<>();
        this.unusedTokenPool = new LinkedList<>();
    }

    @Override
    public String getToken(String clientId) {
        // TODO: Validate that clientId doesn't already have an active token
        final String token;
        if (unusedTokenPool.size() > 0) {
            token = unusedTokenPool.poll();
        } else {
            ChannelService channelService = ChannelServiceFactory.getChannelService();
            token = channelService.createChannel(clientId);
        }
        activeTokenMap.put(token, new TokenInfo(clientId));
        return token;
    }

    @Override
    public void releaseToken(String token) {
        if (!activeTokenMap.containsKey(token)) {
            throw new IllegalArgumentException("The token specified is not active. Token: " + token);
        }
        activeTokenMap.remove(token);
        unusedTokenPool.add(token);
    }

    @Override
    public void ping(String token) {
        TokenInfo info = activeTokenMap.get(token);
        if (info == null) {
            throw new IllegalArgumentException("The token specified is not active. Token: " + token);
        }
        info.ping();
    }

    @Override
    public void reset() {
        unusedTokenPool = new LinkedList<>();
    }

    private static class TokenInfo {
        private final String clientId;
        private long lastSeenTs;

        public TokenInfo(String id) {
            this.clientId = id;
            this.lastSeenTs = System.currentTimeMillis();
        }

        public void ping() {
            this.lastSeenTs = System.currentTimeMillis();
        }
    }
}
