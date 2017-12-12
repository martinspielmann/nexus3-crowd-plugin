/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.security;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestException;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.AbstractReadOnlyUserManager;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author justin
 * @author Issa Gorissen
 */
@Named("Crowd")
@Singleton
public class CrowdUserManager extends AbstractReadOnlyUserManager {
    private static final String SOURCE = "Crowd";

    private RestClient restClient;

    @Inject
    public CrowdUserManager(RestClient rc) {
        restClient = Objects.requireNonNull(rc);

        log.info("CrowdUserManager is starting...");
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public String getAuthenticationRealmName() {
        return CrowdAuthenticatingRealm.NAME;
    }

    @Override
    public User getUser(String userId) throws UserNotFoundException {
        try {
            User user = restClient.getUser(userId);
            return completeUserRolesAndSource(user);
        } catch (RestException e) {
            String mesg = "Unable to look up user " + userId;
            log.debug(mesg, e);
            throw new UserNotFoundException(userId, mesg, e);
        }
    }

    @Override
    public Set<String> listUserIds() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<User> listUsers() {
        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setSource(SOURCE);
        return searchUsers(criteria);
    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        if (!SOURCE.equals(criteria.getSource())) {
            return Collections.emptySet();
        }

        try {
            Set<User> result = restClient.searchUsers(criteria.getUserId());

            for (User user : result) {
                completeUserRolesAndSource(user);
            }

            return result;

        } catch (Exception e) {
            log.error("Unable to get userlist", e);
            return Collections.emptySet();
        }
    }



    private User completeUserRolesAndSource(User user) {
        user.setSource(SOURCE);
        user.setRoles(getUsersRoles(user.getUserId()));
        return user;
    }

    private Set<RoleIdentifier> getUsersRoles(String userId) {
        Set<String> roleNames = null;
        try {
            roleNames = restClient.getNestedGroups(userId);
        } catch (Exception e) {
            log.error("Unable to look up user " + userId, e);
            return Collections.emptySet();
        }
        return Sets.newHashSet(Iterables.transform(roleNames, new Function<String, RoleIdentifier>() {
            @Override
            public RoleIdentifier apply(String from) {
                return new RoleIdentifier(SOURCE, from);
            }
        }));
    }

}
