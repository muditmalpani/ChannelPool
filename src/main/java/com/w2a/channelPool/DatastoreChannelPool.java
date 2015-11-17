package com.w2a.channelPool;

import static com.w2a.channelPool.ofy.OfyService.ofy;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.googlecode.objectify.Key;
import com.w2a.channelPool.ofy.DatastoreTokenInfoFactory;
import com.w2a.channelPool.ofy.TokenInfo;
import java.util.List;

public class DatastoreChannelPool implements ChannelPool {

    // TODO: Add memcache support
    @Override
    public String getToken(String clientId) {
        TokenInfo tokenInfo = DatastoreTokenInfoFactory.tokenInfoForClient(clientId);
        if (tokenInfo != null) {
            return tokenInfo.token;
        }

        tokenInfo = DatastoreTokenInfoFactory.randomUnusedToken();
        if (tokenInfo == null) {
            ChannelService channelService = ChannelServiceFactory.getChannelService();
            String token = channelService.createChannel(clientId);
            tokenInfo = new TokenInfo(token, clientId);
        } else {
            tokenInfo.clientId = clientId;
            tokenInfo.lastUpdateTime = System.currentTimeMillis();
            tokenInfo.isAvailable = false;
        }

        ofy().save().entity(tokenInfo);
        return tokenInfo.token;
    }

    @Override
    public void releaseToken(String token) {
        TokenInfo tokenInfo = DatastoreTokenInfoFactory.tokenInfoForToken(token);
        if (tokenInfo == null) {
            throw new IllegalArgumentException("Specified token doesn't exist. Token ID: " + token);
        }
        tokenInfo.clientId = null;
        ofy().save().entity(tokenInfo);
    }

    @Override
    public void releaseUnusedTokens(long timeoutInMillis) {
        long updateTimeThreshold = System.currentTimeMillis() - timeoutInMillis;
        List<TokenInfo> oldTokens = ofy().load()
                .type(TokenInfo.class)
                .filter("isAvailable", false)
                .filter("lastUpdateTime <", updateTimeThreshold)
                .list();
        for (TokenInfo info : oldTokens) {
            info.clientId = null;
            info.isAvailable = true;
        }
        ofy().save().entities(oldTokens);
    }

    @Override
    public void ping(String token) {
        TokenInfo tokenInfo = DatastoreTokenInfoFactory.tokenInfoForToken(token);
        tokenInfo.ping();
        ofy().save().entity(tokenInfo);
    }

    @Override
    public void reset() {
        List<Key<TokenInfo>> keys = ofy().load().type(TokenInfo.class).keys().list();
        ofy().delete().keys(keys).now();
    }

}
