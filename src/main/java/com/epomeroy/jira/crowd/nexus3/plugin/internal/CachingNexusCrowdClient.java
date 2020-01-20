package com.epomeroy.jira.crowd.nexus3.plugin.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import com.epomeroy.jira.crowd.nexus3.plugin.NexusCrowdClient;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CachedToken;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.mapper.CrowdMapper;
import com.google.inject.Inject;

@Singleton
@Named("CachingNexusCrowdClient")
public class CachingNexusCrowdClient implements NexusCrowdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingNexusCrowdClient.class);
    private CrowdProperties props = null;

    private CloseableHttpClient client = null;
    private CacheProvider cache = null;
    private URI serverUri = null;
    private HttpHost host = null;
    private boolean authCacheEnabled = false;

    CachingNexusCrowdClient() {
    }

    @Inject
    public CachingNexusCrowdClient(CrowdProperties props, CacheProvider cache) {
        this.cache = cache;
        this.props = props;
        this.authCacheEnabled = props.isCacheAuthenticationEnabled();
        LOGGER.info("Authentication Cache enabled: " + authCacheEnabled);
        serverUri = URI.create(normalizeCrowdServerUri(props.getServerUrl()));
        host = new HttpHost(serverUri.getHost(), serverUri.getPort(), serverUri.getScheme());
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(props.getConnectTimeout())
                .setSocketTimeout(props.getSocketTimeout())
                .setConnectionRequestTimeout(props.getConnectionRequestTimeout())
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
    protected String normalizeCrowdServerUri(String serverUri) {
        return serverUri.endsWith("/") ? serverUri.substring(0, serverUri.length() - 1) : serverUri;
    }

    // handle CloseableHttpResponse properly
    protected <T> T executeQuery(final HttpUriRequest request, final ResponseHandler<? extends T> responseHandler) {
        try {
            return getClient().execute(host, request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("error executing query", e);
            return null;
        }
    }

    private HttpGet httpGet(String query) {
        HttpGet g = new HttpGet(query);
        addDefaultHeaders(g);
        return g;
    }

    private void addDefaultHeaders(HttpUriRequest g) {
        g.addHeader("Content-Type", "application/json");
        g.addHeader("Accept", "application/json");
        g.addHeader("Authorization", String.format("Basic %s", Base64.getEncoder().encodeToString(props.getBasicAuthorization())));
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
        if (isAuthCacheEnabled() && authenticateFromCache(token)) {
            return true;
        }

        // if authentication with cached value fails or is skipped, crowd and check auth
        String authRequest = CrowdMapper.toAuthenticationJsonString(token);
        String authResponse = executeQuery(httpPost(buildRestUri("auth", "1", "session"), new StringEntity(authRequest, ContentType.APPLICATION_JSON)), CrowdMapper::toAuthToken);

        if (StringUtils.hasText(authResponse)) {
            // authentication was successful
            if (isAuthCacheEnabled()) {
                getCache().putToken(token.getUsername(), createCachedToken(token.getPassword()));
            }
            return true;
        }
        // authentication failed
        return false;
    }

    protected boolean authenticateFromCache(UsernamePasswordToken token) {
        Optional<CachedToken> cachedToken = cache.getToken(token.getUsername());
        if (cachedToken.isPresent()) {
            // check password
            boolean isPasswordValid = PasswordHasher.isPasswordCorrect(token.getPassword(), cachedToken.get().salt, cachedToken.get().hash);
            if (isPasswordValid) {
                LOGGER.info("Authenticated using cached credentials");
                return true;
            }
        }
        return false;
    }

    protected CachedToken createCachedToken(char[] input) {
        byte[] salt = PasswordHasher.getNextSalt();
        return new CachedToken(PasswordHasher.hash(input, salt), salt);
    }

    protected CloseableHttpClient getClient() {
        return client;
    }

    @Override
    public Set<String> findRolesByUser(String username) {
        Optional<Set<String>> cachedGroups = cache.getGroups(username);
        if (cachedGroups.isPresent()) {
            LOGGER.debug("return groups from cache");
            return cachedGroups.get();
        }

        String restUri = buildRestUri("api", "2", String.format("user?username=%s", encodeUrlParameter(username)));
        LOGGER.debug("getting groups from " + restUri);
        return executeQuery(httpGet(restUri), CrowdMapper::toRoleStrings);
    }

    @Override
    public User findUserByUsername(String username) {
        return executeQuery(httpGet(buildRestUri("api", "2", String.format("user?username=%s", encodeUrlParameter(username)))), CrowdMapper::toUser);
    }

    @Override
    public Role findRoleByRoleId(String roleId) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public Set<String> findAllUsernames() {
        return findUsers().stream().map(User::getUserId).collect(Collectors.toSet());
    }

    @Override
    // TODO: 1/20/20 group member search only returns 50 users at a time. This call needs to query the server until all group members are returned
    public Set<User> findUsers() {
        return executeQuery(httpGet(buildRestUri("api", "2", String.format("group/member?groupname=%s", encodeUrlParameter(props.getJiraUserGroup())))), CrowdMapper::toUsers);
    }

    @Override
    public Set<User> findUserByCriteria(UserSearchCriteria criteria) {
        Set<User> users = findUsers();

        return users.stream()
                .filter(user -> user.getName().startsWith(criteria.getUserId())
                                || user.getFirstName().startsWith(criteria.getUserId())
                                || user.getLastName().startsWith(criteria.getUserId())
                                || user.getEmailAddress().startsWith(criteria.getUserId()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Role> findRoles() {
        return CrowdMapper.toRoles(props.getJiraUserGroup());
    }

    protected String buildRestUri(String apiName, String apiVersion, String path) {
        return String.format("%s/rest/%s/%s/%s", getServerUriString(), apiName, apiVersion, path);
    }

    protected String getServerUriString() {
        return serverUri.toString();
    }

    protected boolean isAuthCacheEnabled() {
        return authCacheEnabled;
    }

    protected CacheProvider getCache() {
        return cache;
    }

    protected String encodeUrlParameter(String string) {
        try {
            return URLEncoder.encode(string, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("ouch... your platform does not support utf-8?", e);
            return "";
        }
    }
}
