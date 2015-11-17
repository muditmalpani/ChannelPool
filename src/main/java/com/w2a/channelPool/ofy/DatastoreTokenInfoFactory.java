package com.w2a.channelPool.ofy;

import static com.w2a.channelPool.ofy.OfyService.ofy;

public class DatastoreTokenInfoFactory {
    public static TokenInfo tokenInfoForClient(String clientId) {
        return ofy().load()
                .type(TokenInfo.class)
                .filter("clientId", clientId)
                .first()
                .now();
    }
    
    public static TokenInfo tokenInfoForToken(String token) {
        return ofy().load()
                .type(TokenInfo.class)
                .id(token)
                .now();
    }
    
    public static TokenInfo randomUnusedToken() {
        return ofy().load()
                .type(TokenInfo.class)
                .filter("isAvailable", true)
                .first()
                .now();
    }
}
