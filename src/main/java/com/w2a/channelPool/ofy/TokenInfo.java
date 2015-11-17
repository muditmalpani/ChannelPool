package com.w2a.channelPool.ofy;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class TokenInfo {

    @Id
    public String token;
    @Index
    public String clientId;
    @Index
    public long lastUpdateTime;
    @Index
    public boolean isAvailable;

    // Empty constructor needed by Objectify
    public TokenInfo() {
    }

    public TokenInfo(String token, String clientId) {
        this.token = token;
        this.clientId = clientId;
        this.lastUpdateTime = System.currentTimeMillis();
        this.isAvailable = false;
    }

    public void ping() {
        this.lastUpdateTime = System.currentTimeMillis();
    }
}
