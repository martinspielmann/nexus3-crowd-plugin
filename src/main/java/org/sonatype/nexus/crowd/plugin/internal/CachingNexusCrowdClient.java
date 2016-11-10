package org.sonatype.nexus.crowd.plugin.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.crowd.plugin.NexusCrowdClient;
import org.sonatype.nexus.crowd.plugin.internal.entity.mapper.CrowdMapper;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import com.google.common.base.Strings;
import com.google.inject.Inject;

@Singleton
@Named("CachingNexusCrowdClient")
public class CachingNexusCrowdClient implements NexusCrowdClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingNexusCrowdClient.class);

	private final CloseableHttpClient client;
	private final CacheProvider cache;
	private final URI serverUri;
	private HttpHost host;

	@Inject
	public CachingNexusCrowdClient(CrowdProperties props, CacheProvider cache) {
		this.cache = cache;
		// check if crowd url ends with a "/" and if so, cut it. fixes #9
		serverUri = URI.create(
				props.getServerUrl().endsWith("/")?
						props.getServerUrl().substring(0, props.getServerUrl().length()-1):
							props.getServerUrl());
		host = new HttpHost(serverUri.getHost(), serverUri.getPort(), serverUri.getScheme());

		// TODO get various timeouts from environment / system properties / custome properties
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(15000)
				.setSocketTimeout(15000)
				.setConnectionRequestTimeout(15000)
				.build();

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(serverUri.getHost(), serverUri.getPort()),
				new UsernamePasswordCredentials(props.getApplicationName(), props.getApplicationPassword()));
		client = HttpClientBuilder.create().setDefaultRequestConfig(defaultRequestConfig).setDefaultCredentialsProvider(credentialsProvider).build();
	}

	// handle CloseableHttpResponse properly
	private <T> T executeQuery(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) {
		try {
			return getClient().execute(host, request, responseHandler);
		} catch (IOException e) {
			LOGGER.error("error executng query", e);
		}
		return null;
	}

	private HttpGet httpGet(String query) {
		HttpGet g = new HttpGet(query);
		addDefaultHeaders(g);
		return g;
	}

	protected void addDefaultHeaders(HttpUriRequest g) {
		g.addHeader("X-Atlassian-Token", "nocheck");
		g.addHeader("Accept", "application/json");
	}

	private HttpPost httpPost(String query, HttpEntity entity) {
		HttpPost p = new HttpPost(query);
		addDefaultHeaders(p);
		p.setEntity(entity);
		return p;
	}

	@Override
	public boolean authenticate(UsernamePasswordToken token) {
		// check if token is cached
		Optional<String> cachedToken = cache.getToken(token.getUsername());
		if (cachedToken.isPresent()) {
			return true;
		}
		String auth = executeQuery(
				httpPost(restUri("session"),
						new StringEntity(
								CrowdMapper.toUsernamePasswordJsonString(token.getUsername(), token.getPassword()),
								ContentType.APPLICATION_JSON)), CrowdMapper::toAuthToken);

		if (Strings.isNullOrEmpty(auth)) {
			return false;
		} else {
			cache.putToken(token.getUsername(), auth);
			return true;
		}

	}

	protected CloseableHttpClient getClient() {
		return client;
	}

	@Override
	public Set<String> findRolesByUser(String username) {
		Optional<Set<String>> cachedGroups = cache.getGroups(username);
		if (cachedGroups.isPresent()) {
			LOGGER.info("return groups from cache");
			return cachedGroups.get();
		}
		String restUri = restUri(String.format("user/group/nested?username=%s", username));
		LOGGER.info("getting groups from "+restUri);
		return executeQuery(httpGet(restUri), CrowdMapper::toRoleStrings);
	}

	@Override
	public User findUserByUsername(String username) {
		return executeQuery(httpGet(restUri(String.format("user?username=%s", username))), CrowdMapper::toUser);
	}

	@Override
	public Role findRoleByRoleId(String roleId) {
		return executeQuery(httpGet(restUri(String.format("group?groupname=%s", roleId))), CrowdMapper::toRole);
	}

	@Override
	public Set<String> findAllUsernames() {
		return findUsers().stream().map(User::getUserId).collect(Collectors.toSet());
	}

	@Override
	public Set<User> findUsers() {
		return executeQuery(httpGet(restUri("search?entity-type=user&expand=user")), CrowdMapper::toUsers);
	}

	@Override
	public Set<User> findUserByCriteria(UserSearchCriteria criteria) {
		String query = createQueryFromCriteria(criteria);
		return executeQuery(httpGet(restUri(String.format("search?entity-type=user&expand=user&restriction=%s", query))), CrowdMapper::toUsers);
	}

	private String createQueryFromCriteria(UserSearchCriteria criteria) {
		StringBuilder query = new StringBuilder("active=true");
		if (!Strings.isNullOrEmpty(criteria.getUserId())) {
			query.append(" AND name=\"").append(criteria.getUserId()).append("*\"");
		}
		if (!Strings.isNullOrEmpty(criteria.getEmail())) {
			query.append(" AND email=\"").append(criteria.getEmail()).append("\"");
		}
		try {
			return URLEncoder.encode(query.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("ouch... your platform does not support utf-8?", e);
			return "";

		}
	}

	@Override
	public Set<Role> findRoles() {
		return executeQuery(httpGet(restUri("search?entity-type=group&expand=group")), CrowdMapper::toRoles);
	}



	private String restUri(String path) {
		return String.format("%s/rest/usermanagement/1/%s", serverUri.toString(), path);
	}
}
