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
package com.epomeroy.jira.crowd.nexus3.plugin.internal;

import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.AbstractReadOnlyUserManager;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import com.epomeroy.jira.crowd.nexus3.plugin.CrowdAuthenticatingRealm;
import com.google.inject.Inject;

/**
 * @author justin
 * @author Issa Gorissen
 */
@Singleton
@Typed(UserManager.class)
@Named("Crowd")
public class CrowdUserManager extends AbstractReadOnlyUserManager {

    public static final String SOURCE = "Crowd";
    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdUserManager.class);

    private CachingNexusCrowdClient client;

    CrowdUserManager() {
    }

    @Inject
    public CrowdUserManager(CachingNexusCrowdClient client) {
        LOGGER.info("CrowdUserManager is starting...");
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthenticationRealmName() {
        return CrowdAuthenticatingRealm.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSource() {
        return SOURCE;
    }

    private User completeUserRolesAndSource(User user) {
        user.setSource(SOURCE);
        Set<String> roles = client.findRolesByUser(user.getUserId());
        user.setRoles(roles.stream().map(r -> new RoleIdentifier(SOURCE, r)).collect(Collectors.toSet()));
        return user;
    }

    @Override
    public Set<User> listUsers() {
        return client.findUsers().stream().map(this::completeUserRolesAndSource).collect(Collectors.toSet());
    }

    @Override
    public Set<String> listUserIds() {
        return client.findAllUsernames();
    }

    @Override
    public Set<User> searchUsers(UserSearchCriteria criteria) {
        return client.findUserByCriteria(criteria).stream().map(this::completeUserRolesAndSource).collect(Collectors.toSet());
    }

    @Override
    public User getUser(String userId) throws UserNotFoundException {
        User u = client.findUserByUsername(userId);
        if (u == null) {
            throw new UserNotFoundException(userId);
        }
        return completeUserRolesAndSource(u);
    }

}
