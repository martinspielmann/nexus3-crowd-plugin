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

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestException;

@Singleton
@Named(CrowdAuthenticatingRealm.NAME)
@Description("OSS Crowd Authentication Realm")
public class CrowdAuthenticatingRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(CrowdAuthenticatingRealm.class);

    public static final String NAME = "NexusCrowdAuthenticationRealm";

    private RestClient restClient;

    @Inject
    public CrowdAuthenticatingRealm(RestClient rc) {
        restClient = Objects.requireNonNull(rc);
        setName(NAME);

        LOG.info("CrowdAuthenticatingRealm is starting...");
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        if (!(authenticationToken instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException("Token of type " + authenticationToken.getClass().getName()
                    + " is not supported.  A " + UsernamePasswordToken.class.getName() + " is required.");
        }
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

        String password = new String(token.getPassword());

        try {
            restClient.authenticate(token.getUsername(), password);
            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
        } catch (RestException re) {
            throw new AccountException("Invalid login credentials for user '" + token.getUsername() + "'");
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (principals == null) {
            throw new AuthorizationException("Cannot authorize with no principals.");
        }

        String username = principals.getPrimaryPrincipal().toString();
        try {
            Set<String> groups = restClient.getNestedGroups(username);
            return new SimpleAuthorizationInfo(groups);
        } catch (Exception e) {
            throw new AuthorizationException(String.format("Problems while sending get nested groups of user '%s'", username), e);
        }
    }

}

