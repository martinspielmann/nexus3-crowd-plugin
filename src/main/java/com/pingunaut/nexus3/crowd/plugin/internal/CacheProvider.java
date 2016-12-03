package com.pingunaut.nexus3.crowd.plugin.internal;

import java.util.Optional;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Singleton
@Named("CrowdCacheProvider")
public class CacheProvider {

	private static final String TOKEN_CACHE_NAME = "crowd_plugin_tokens";
	private static final String RESPONSES_CACHE_NAME = "crowd_plugin_responses";
	private static final String GROUPS_KEY_PREFIX = "groups_";

	// cache lifetime 15m
	private static final int TTL_SECONDS = 3600/4;
	// max cached elements 32000
	private static final int MAX_CACHED_ELEMENTS = 32000;


	public void putToken(String username, CachedToken crowdToken) {
		tokenCache().put(new Element(username, crowdToken));
	}

	private Cache tokenCache(){
		if (!CacheManager.getInstance().cacheExists(TOKEN_CACHE_NAME)) {
			CacheManager.getInstance().addCache(newCache(TOKEN_CACHE_NAME));
		}
		return CacheManager.getInstance().getCache(TOKEN_CACHE_NAME);
	}

	private Cache responseCache(){
		if (!CacheManager.getInstance().cacheExists(RESPONSES_CACHE_NAME)) {
			CacheManager.getInstance().addCache(newCache(RESPONSES_CACHE_NAME));
		}
		return CacheManager.getInstance().getCache(RESPONSES_CACHE_NAME);

	}

	private Cache newCache(String name){
		return new Cache(name, MAX_CACHED_ELEMENTS, false, false, TTL_SECONDS, TTL_SECONDS);

	}

	public Optional<CachedToken> getToken(String username) {
		Element element = tokenCache().get(username);
		if(element!=null){
			return Optional.ofNullable((CachedToken) element.getObjectValue());
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public Optional<Set<String>> getGroups(String username) {
		Element element = responseCache().get(GROUPS_KEY_PREFIX+username);
		if(element!=null){
			return Optional.ofNullable((Set<String>) element.getObjectValue());
		}
		return Optional.empty();
	}

	public void putGroups(String username, Set<String> groups) {
		responseCache().put(new Element(GROUPS_KEY_PREFIX+username, groups));
	}
}
