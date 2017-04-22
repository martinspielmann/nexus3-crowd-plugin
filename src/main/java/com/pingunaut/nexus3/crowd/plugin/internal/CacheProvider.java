package com.pingunaut.nexus3.crowd.plugin.internal;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.ValueSupplier;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;

@Singleton
@Named("CrowdCacheProvider")
public class CacheProvider {

    private static final String TOKEN_CACHE_NAME = "crowd_plugin_tokens";
    private static final String RESPONSES_CACHE_NAME = "crowd_plugin_responses";
    private static final String GROUPS_KEY_PREFIX = "groups_";

    // cache lifetime 15m
    private static final int TTL_SECONDS = 3600 / 4;

    private Cache<String, CachedToken> tokenCache;
    private Cache<String, Set> responseCache;

    private CacheManager manager;

    public CacheProvider() {
        manager = CacheManagerBuilder.newCacheManagerBuilder().build();
        manager.init();

        tokenCache = createTokenCache();
        responseCache = createResponseCache();
    }

    public void putToken(String username, CachedToken crowdToken) {
        tokenCache.put(username, crowdToken);
    }

    private Cache<String, Set> createResponseCache() {
        return manager.createCache(RESPONSES_CACHE_NAME, createCacheConfig(String.class, Set.class));
    }


    private Cache<String, CachedToken> createTokenCache() {
        return manager.createCache(TOKEN_CACHE_NAME, createCacheConfig(String.class, CachedToken.class));
    }

    public Optional<CachedToken> getToken(String username) {
        CachedToken element = tokenCache.get(username);
        return Optional.ofNullable(element);
    }

    @SuppressWarnings("unchecked")
    public Optional<Set<String>> getGroups(String username) {
        Set<String> element = responseCache.get(GROUPS_KEY_PREFIX + username);
        return Optional.ofNullable(element);
    }

    public void putGroups(String username, Set<String> groups) {
        responseCache.put(GROUPS_KEY_PREFIX + username, groups);
    }

    private <K, V> CacheConfigurationBuilder<K, V> createCacheConfig(Class<K> keyClass, Class<V> valueClass) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(keyClass, valueClass, ResourcePoolsBuilder
                .heap(100))
                .withExpiry(new Expiry<K, V>() {
                    @Override
                    public Duration getExpiryForCreation(K key, V value) {
                        return Duration.of(TTL_SECONDS, TimeUnit.SECONDS);
                    }

                    @Override
                    public Duration getExpiryForAccess(K key, ValueSupplier<? extends V> value) {
                        return null;  // Keeping the existing expiry
                    }

                    @Override
                    public Duration getExpiryForUpdate(K key, ValueSupplier<? extends V> oldValue, V newValue) {
                        return null;  // Keeping the existing expiry
                    }
                });
    }
}
