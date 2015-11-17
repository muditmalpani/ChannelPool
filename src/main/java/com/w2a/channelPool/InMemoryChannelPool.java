package com.w2a.channelPool;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.w2a.channelPool.ofy.TokenInfo;
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

    /*
     * This map is used to make sure that there is one-to-one mapping between
     * client id and token
     */
    private Map<String, TokenInfo> clientIdToTakenMap;

    private Queue<String> unusedTokenPool;

    public InMemoryChannelPool() {
        this.activeTokenMap = new HashMap<>();
        this.unusedTokenPool = new LinkedList<>();
    }

    @Override
    public String getToken(String clientId) {
        // If the client is already assigned a token, return the same token
        // Else, return a token from the pool or generate a new token if
        // pool is empty
        if (clientIdToTakenMap.containsKey(clientId)) {
            return clientIdToTakenMap.get(clientId).token;
        }
        final String token;
        if (unusedTokenPool.size() > 0) {
            token = unusedTokenPool.poll();
        } else {
            ChannelService channelService = ChannelServiceFactory.getChannelService();
            token = channelService.createChannel(clientId);
        }
        TokenInfo info = new TokenInfo(clientId, token);
        activeTokenMap.put(token, info);
        clientIdToTakenMap.put(clientId, info);
        return token;
    }

    @Override
    public void releaseToken(String token) {
        TokenInfo info = activeTokenMap.get(token);
        if (info == null) {
            throw new IllegalArgumentException("The token specified is not active. Token: " + token);
        }
        activeTokenMap.remove(token);
        clientIdToTakenMap.remove(info.clientId);
        unusedTokenPool.add(token);
    }

    @Override
    public void releaseUnusedTokens(long timeoutInMillis) {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, TokenInfo> entry : activeTokenMap.entrySet()) {
            TokenInfo info = entry.getValue();
            if (currentTime - info.lastUpdateTime > timeoutInMillis) {
                activeTokenMap.remove(info.token);
                clientIdToTakenMap.remove(info.clientId);
                unusedTokenPool.add(info.token);
            }
        }
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
}
