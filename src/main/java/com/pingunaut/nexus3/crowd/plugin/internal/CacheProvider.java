package com.pingunaut.nexus3.crowd.plugin.internal;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.cache.configuration.Configuration;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.ValueSupplier;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.config.BaseCacheConfiguration;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;
import org.sonatype.nexus.cache.internal.ehcache.EhCacheManagerProvider;

@Singleton
@Named("CrowdCacheProvider")
public class CacheProvider {

    private static final String TOKEN_CACHE_NAME = "crowd_plugin_tokens";
    private static final String RESPONSES_CACHE_NAME = "crowd_plugin_responses";
    private static final String GROUPS_KEY_PREFIX = "groups_";

    // cache lifetime 15m
    private static final int TTL_SECONDS = 3600 / 4;

    private EhCacheManagerProvider provider;

    private Cache<String, CachedToken> tokenCache;
    private Cache<String, Set> responseCache;

    @Inject
    public CacheProvider(EhCacheManagerProvider provider) {
        this.provider = provider;
        tokenCache = createTokenCache();
        responseCache = createResponseCache();
    }

    public void putToken(String username, CachedToken crowdToken) {
        tokenCache.put(username, crowdToken);
    }

    private Cache<String, Set> createResponseCache() {
        CacheConfigurationBuilder<String, Set> c =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Set.class, ResourcePoolsBuilder
                        .heap(100))
                        .withExpiry(new Expiry<String, Set>() {
                            @Override
                            public Duration getExpiryForCreation(String key, Set value) {
                                return Duration.of(TTL_SECONDS, TimeUnit.SECONDS);
                            }

                            @Override
                            public Duration getExpiryForAccess(String key, ValueSupplier<? extends Set> value) {
                                return null;  // Keeping the existing expiry
                            }

                            @Override
                            public Duration getExpiryForUpdate(String key, ValueSupplier<? extends Set> oldValue, Set newValue) {
                                return null;  // Keeping the existing expiry
                            }
                        });

        return cacheManager().createCache(RESPONSES_CACHE_NAME, c);
    }

    private Cache<String, CachedToken> createTokenCache() {
        CacheConfigurationBuilder<String, CachedToken> c =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CachedToken.class, ResourcePoolsBuilder
                        .heap(100))
                        .withExpiry(new Expiry<String, CachedToken>() {
                            @Override
                            public Duration getExpiryForCreation(String key, CachedToken value) {
                                return Duration.of(TTL_SECONDS, TimeUnit.SECONDS);
                            }

                            @Override
                            public Duration getExpiryForAccess(String key, ValueSupplier<? extends CachedToken> value) {
                                return null;  // Keeping the existing expiry
                            }

                            @Override
                            public Duration getExpiryForUpdate(String key, ValueSupplier<? extends CachedToken> oldValue, CachedToken newValue) {
                                return null;  // Keeping the existing expiry
                            }
                        });
        return cacheManager().createCache(TOKEN_CACHE_NAME, c);
    }

    private CacheManager cacheManager(){
        return (CacheManager)provider.get();
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
}
