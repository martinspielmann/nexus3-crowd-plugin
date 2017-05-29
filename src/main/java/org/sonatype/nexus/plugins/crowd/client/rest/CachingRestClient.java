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
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.ValueSupplier;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;
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

    private static final String GROUPS_CACHE_NAME = "com.atlassian.crowd.restresponse.cache.groups";
    private static final String USERS_CACHE_NAME = "com.atlassian.crowd.restresponse.cache.users";

    private Cache<String, Set> groupsCache;
    private Cache<String, User> userCache;

    @Inject
    public CachingRestClient(CrowdPluginConfiguration config) throws URISyntaxException {
        super(config);
        CacheManager ehCacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
        ehCacheManager.init();
        groupsCache = ehCacheManager.createCache(GROUPS_CACHE_NAME, createCacheConfig(String.class, Set.class, config));
        userCache = ehCacheManager.createCache(USERS_CACHE_NAME, createCacheConfig(String.class, User.class, config));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getNestedGroups(String username) throws RemoteException {
        String key = "nestedgroups" + username;
        Set<String> elem = groupsCache.get(key);
        if (elem != null) {
            LOG.debug("getNestedGroups({}) from cache", username);
            return elem;
        }

        Set<String> groups = super.getNestedGroups(username);
        groupsCache.put(key, groups);
        return groups;
    }

    @Override
    public User getUser(String userid) throws RemoteException {
        String key = "user" + userid;
        User elem = userCache.get(key);
        if (elem != null) {
            LOG.debug("getUser({}) from cache", userid);
            return elem;
        }

        User user = super.getUser(userid);
        userCache.put(key, user);
        return user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Role> getAllGroups() throws RemoteException {
        String key = "allgroups";
        Set<Role> elem = groupsCache.get(key);
        if (elem != null) {
            LOG.debug("getAllGroups from cache");
            return elem;
        }

        Set<Role> groups = super.getAllGroups();
        groupsCache.put(key, groups);
        return groups;
    }

    private <K, V> CacheConfigurationBuilder<K, V> createCacheConfig(Class<K> keyClass, Class<V> valueClass, CrowdPluginConfiguration config) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(keyClass, valueClass, ResourcePoolsBuilder
                .heap(100))
                .withExpiry(new Expiry<K, V>() {
                    @Override
                    public Duration getExpiryForCreation(K key, V value) {
                        return Duration.of(config.getCacheTTL(), TimeUnit.SECONDS);
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
