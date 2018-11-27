/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client.rest;

import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;

/**
 * @author Issa Gorissen
 */
@Named
@Singleton
public class CachingRestClient extends RestClient {
	private static final Logger LOG = LoggerFactory.getLogger(CachingRestClient.class);

	private static final String GROUPS_CACHE_NAME = CachingRestClient.class.getName() + "#cache.groups";
	private static final String USERS_CACHE_NAME = CachingRestClient.class.getName() + "#cache.users";
	private static final String AUTH_CACHE_NAME = CachingRestClient.class.getName() + "#cache.auths";
	private static final String KEY_ALL_GROUPS = CachingRestClient.class.getName() + "#allgroups";

	private static final int DEFAULT_CACHE_HEAP_SIZE = 1000;
	
	private CacheManager ehCacheManager;
	private Cache<String, User> userCache;
	private Cache<String, String> authCache;

	@SuppressWarnings("rawtypes")
	private Cache<String, Set> groupsCache;

	@Inject
	public CachingRestClient(CrowdPluginConfiguration config) throws URISyntaxException {
		super(config);

		ehCacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		ehCacheManager.init();
		groupsCache = ehCacheManager.createCache(GROUPS_CACHE_NAME, createCacheConfig(String.class, Set.class, config));
		userCache = ehCacheManager.createCache(USERS_CACHE_NAME, createCacheConfig(String.class, User.class, config));

		// for auth cache, we use idle time instead of live time
		authCache = ehCacheManager.createCache(AUTH_CACHE_NAME,
				CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(DEFAULT_CACHE_HEAP_SIZE))
						.withExpiry(Expirations.timeToIdleExpiration(Duration.of(5, TimeUnit.MINUTES))).build());
	}

	@Override
	protected void finalize() throws Throwable {
		ehCacheManager.close();
		super.finalize();
	}

	@Override
	public Set<String> getNestedGroups(String username) throws RestException {
		@SuppressWarnings("unchecked")
		Set<String> elem = groupsCache.get(username);
		if (elem != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("getNestedGroups({}) from cache", username);
			}
			return elem;
		}

		Set<String> groups = super.getNestedGroups(username);
		groupsCache.put(username, groups);
		return groups;
	}

	@Override
	public User getUser(String username) throws RestException {
		if (username.equals("null")) {
			// NX is using username null as guest access or something...
			throw new RestException("user null does not exist in Crowd");
		}
		
		User elem = userCache.get(username);
		if (elem != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("getUser({}) from cache", username);
			}
			return elem;
		}

		User user = super.getUser(username);
		userCache.put(username, user);
		return user;
	}

	@Override
	public Set<Role> getAllGroups() throws RestException {
		@SuppressWarnings("unchecked")
		Set<Role> elem = groupsCache.get(KEY_ALL_GROUPS);
		if (elem != null) {
			LOG.debug("getAllGroups from cache");
			return elem;
		}

		Set<Role> groups = super.getAllGroups();
		groupsCache.put(KEY_ALL_GROUPS, groups);
		return groups;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void authenticate(String username, String password) throws RestException {
		String cachedPasswordHash = authCache.get(username);
		String passwordHash = DigestUtils.sha512Hex(password);
		if (passwordHash.equals(cachedPasswordHash)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("user {} password compared with cached hash successfully", username);
			}
			return;
		}

		super.authenticate(username, password);
		authCache.put(username, passwordHash);
	}

	private static <K, V> CacheConfigurationBuilder<K, V> createCacheConfig(Class<K> keyClass, Class<V> valueClass,
			CrowdPluginConfiguration config) {
		return CacheConfigurationBuilder
				.newCacheConfigurationBuilder(keyClass, valueClass, ResourcePoolsBuilder.heap(DEFAULT_CACHE_HEAP_SIZE))
				.withExpiry(Expirations.timeToLiveExpiration(Duration.of(config.getCacheTTL(), TimeUnit.SECONDS)));
	}
}
