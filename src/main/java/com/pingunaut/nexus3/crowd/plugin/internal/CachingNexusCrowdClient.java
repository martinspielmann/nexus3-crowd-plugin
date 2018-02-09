package com.pingunaut.nexus3.crowd.plugin.internal;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.pingunaut.nexus3.crowd.plugin.NexusCrowdClient;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.mapper.CrowdMapper;
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
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named("CachingNexusCrowdClient")
public class CachingNexusCrowdClient implements NexusCrowdClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(CachingNexusCrowdClient.class);

	private final CloseableHttpClient client;
	private final CacheProvider cache;
	private final URI serverUri;
	private final HttpHost host;
	private final boolean authCacheEnabled;

	@Inject
	public CachingNexusCrowdClient(CrowdProperties props, CacheProvider cache) {
		this.cache = cache;
		this.authCacheEnabled = props.isCacheAuthenticationEnabled();
        LOGGER.info("Authentication Cache enabled: " + authCacheEnabled);
		serverUri = URI.create(normalizeCrowdServerUri(props.getServerUrl()));
		host = new HttpHost(serverUri.getHost(), serverUri.getPort(), serverUri.getScheme());
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(15000)
				.setSocketTimeout(15000)
				.setConnectionRequestTimeout(15000)
				.build();
		UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(props.getApplicationName(), props.getApplicationPassword());
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(serverUri.getHost(), serverUri.getPort()), usernamePasswordCredentials);
		client = HttpClientBuilder.create().setDefaultRequestConfig(defaultRequestConfig).setDefaultCredentialsProvider(credentialsProvider).build();
	}

    /**
     * Check if crowd url ends with a "/" and if so, cut it. fixes #9
     *
     * @param serverUri
     * @return the normalized server uri
     */
	protected String normalizeCrowdServerUri(String serverUri){
        return serverUri.endsWith("/") ? serverUri.substring(0, serverUri.length() - 1) : serverUri;
	}

	// handle CloseableHttpResponse properly
	private <T> T executeQuery(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) {
		try {
			return getClient().execute(host, request, responseHandler);
		} catch (IOException e) {
			LOGGER.error("error executng query", e);
			return null;
		}
	}

	private HttpGet httpGet(String query) {
		HttpGet g = new HttpGet(query);
		addDefaultHeaders(g);
		return g;
	}

	private void addDefaultHeaders(HttpUriRequest g) {
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
		if(authCacheEnabled) {
			Optional<CachedToken> cachedToken = cache.getToken(token.getUsername());
			if (cachedToken.isPresent()) {
				// check password
                boolean isPasswordValid = PasswordHasher.isPasswordCorrect(token.getPassword(), cachedToken.get().salt, cachedToken.get().hash);
				if (isPasswordValid) {
                    LOGGER.info("Authenticated using cached credentials");
					return true;
				}
			}
		}

		// if authentication with cached value fails or is skipped, crowd and check auth
        String authRequest = CrowdMapper.toUsernamePasswordJsonString(token.getUsername(), token.getPassword());
        String authResponse = executeQuery(httpPost(restUri("session"), new StringEntity(authRequest, ContentType.APPLICATION_JSON)), CrowdMapper::toAuthToken);

		if (StringUtils.hasText(authResponse)) {
            // authentication was successful
            if(authCacheEnabled){
                cache.putToken(token.getUsername(), createCachedToken(token.getPassword()));
            }
            return true;
		}

        // authentication failed
        return false;
	}

	private static CachedToken createCachedToken(char[] input)  {
		byte[] salt = PasswordHasher.getNextSalt();
        byte[] hash = PasswordHasher.hash(input, salt);
        return new CachedToken(hash, salt);
	}

	private CloseableHttpClient getClient() {
		return client;
	}

	@Override
	public Set<String> findRolesByUser(String username) {
		Optional<Set<String>> cachedGroups = cache.getGroups(username);
		if (cachedGroups.isPresent()) {
			LOGGER.debug("return groups from cache");
			return cachedGroups.get();
		}
		String restUri = restUri(String.format("user/group/nested?username=%s", username));
		LOGGER.debug("getting groups from "+restUri);
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
			query.append(" AND email=\"").append(criteria.getEmail()).append("*\"");
		}
		try {
			return URLEncoder.encode(query.toString(), StandardCharsets.UTF_8.toString());
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
