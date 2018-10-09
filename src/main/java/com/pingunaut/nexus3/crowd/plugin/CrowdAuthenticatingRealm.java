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
package com.pingunaut.nexus3.crowd.plugin;

import com.pingunaut.nexus3.crowd.plugin.internal.CachingNexusCrowdClient;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The Class CrowdAuthenticatingRealm.
 */
@Singleton
@Named(CrowdAuthenticatingRealm.NAME)
@Description("Crowd Authentication Realm")
public class CrowdAuthenticatingRealm extends AuthorizingRealm {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrowdAuthenticatingRealm.class);
	public static final String NAME = "CrowdAuthenticatingRealm";
	private CachingNexusCrowdClient client;

	/**
	 * Instantiates a new crowd authenticating realm.
	 *
	 * @param client
	 *            the client
	 */
	@Inject
	public CrowdAuthenticatingRealm(final CachingNexusCrowdClient client) {
		this.client = client;
		setName(NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.shiro.realm.AuthorizingRealm#onInit()
	 */
	@Override
	protected void onInit() {
		super.onInit();
		LOGGER.info("Crowd Realm initialized...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache
	 * .shiro.subject.PrincipalCollection)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String) principals.getPrimaryPrincipal();
		LOGGER.info("doGetAuthorizationInfo for " + username);
		return new SimpleAuthorizationInfo(client.findRolesByUser(username));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.
	 * apache.shiro.authc.AuthenticationToken)
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (!(token instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s is required.",
					token.getClass().getName(), UsernamePasswordToken.class.getName()));
		}

		UsernamePasswordToken t = (UsernamePasswordToken) token;
		LOGGER.info("doGetAuthenticationInfo for " + t.getUsername());
		boolean authenticated = client.authenticate(t);
		LOGGER.info("crowd authenticated: " + authenticated);

		if (authenticated) {
			return createSimpleAuthInfo(t);
		} else {
			return null;
		}
	}

	/**
	 * Creates the simple auth info.
	 *
	 * @param token
	 *            the token
	 * @return the simple authentication info
	 */
	private SimpleAuthenticationInfo createSimpleAuthInfo(UsernamePasswordToken token) {
		return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), NAME);
	}

}
