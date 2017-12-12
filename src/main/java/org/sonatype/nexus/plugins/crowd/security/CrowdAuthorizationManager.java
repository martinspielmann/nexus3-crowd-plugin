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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestException;
import org.sonatype.nexus.security.authz.AbstractReadOnlyAuthorizationManager;
import org.sonatype.nexus.security.privilege.NoSuchPrivilegeException;
import org.sonatype.nexus.security.privilege.Privilege;
import org.sonatype.nexus.security.role.NoSuchRoleException;
import org.sonatype.nexus.security.role.Role;

/**
 * @author justin
 * @author Issa Gorissen
 */
@Singleton
@Named("Crowd")
public class CrowdAuthorizationManager extends AbstractReadOnlyAuthorizationManager {
    private static final Logger LOG = LoggerFactory.getLogger(CrowdAuthorizationManager.class);

    private static final String SOURCE = "Crowd";

    private RestClient restClient;

    @Inject
    public CrowdAuthorizationManager(RestClient rc) {
        restClient = Objects.requireNonNull(rc);

        LOG.info("CrowdAuthorizationManager is starting...");
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public Role getRole(String roleId) throws NoSuchRoleException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Set<Role> listRoles() {
        try {
            Set<Role> roles = restClient.getAllGroups();
            for (Role role : roles) {
                role.setSource(getSource());
            }
            return roles;
        } catch (RestException e) {
            LOG.error("Unable to load roles", e);
            return null;
        }
    }

    @Override
    public Set<Privilege> listPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
        throw new NoSuchPrivilegeException(privilegeId);
    }

}
