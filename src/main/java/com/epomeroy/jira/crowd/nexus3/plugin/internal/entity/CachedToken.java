package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity;

/**
 * Created by m on 12/3/16.
 */
public class CachedToken {
    public final byte[] hash;
    public final byte[] salt;

    public CachedToken(byte[] hash, byte[] salt) {
        this.hash = hash;
        this.salt = salt;
    }
}
