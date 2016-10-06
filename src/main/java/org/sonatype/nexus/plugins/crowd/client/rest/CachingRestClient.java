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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

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

    private static final String REST_RESPONSE_CACHE = "com.atlassian.crowd.restresponse.cache";

    private CacheManager ehCacheManager;

    @Inject
    public CachingRestClient(CrowdPluginConfiguration config) throws URISyntaxException {
        super(config);

        ehCacheManager = CacheManager.getInstance();
        // create a cache with max items = 10000 and TTL (live and idle) = 1 hour
        Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, config.getCacheTTL(), config.getCacheTTL());
        ehCacheManager.addCacheIfAbsent(cache);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getNestedGroups(String username) throws RemoteException {
        Cache cache = getCache();
        String key = "nestedgroups" + username;
        Element elem = cache.get(key);
        if (elem != null) {
            LOG.debug("getNestedGroups({}) from cache", username);
            return (Set<String>) elem.getObjectValue();
        }

        Set<String> groups = super.getNestedGroups(username);
        cache.put(new Element(key, groups));
        return groups;
    }

    @Override
    public User getUser(String userid) throws RemoteException {
        Cache cache = getCache();
        String key = "user" + userid;
        Element elem = cache.get(key);
        if (elem != null) {
            LOG.debug("getUser({}) from cache", userid);
            return (User) elem.getObjectValue();
        }

        User user = super.getUser(userid);
        cache.put(new Element(key, user));
        return user;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Role> getAllGroups() throws RemoteException {
        Cache cache = getCache();
        String key = "allgroups";
        Element elem = cache.get(key);
        if (elem != null) {
            LOG.debug("getAllGroups from cache");
            return (Set<Role>) elem.getObjectValue();
        }

        Set<Role> groups = super.getAllGroups();
        cache.put(new Element(key, groups));
        return groups;
    }

    private Cache getCache() {
        return ehCacheManager.getCache(REST_RESPONSE_CACHE);
    }
}
