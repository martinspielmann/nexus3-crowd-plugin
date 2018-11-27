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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.AuthenticatePost;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.ConfigCookieGetResponse;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.GroupResponse;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.GroupsResponse;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.SearchUserGetResponse;
import org.sonatype.nexus.plugins.crowd.client.rest.jaxb.UserResponse;
import org.sonatype.nexus.plugins.crowd.config.CrowdPluginConfiguration;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

/**
 * @author Issa Gorissen
 */
public class RestClient {
    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);
    private static final String UTF8 = "UTF-8";

    private HttpClient client;
    private Credentials crowdCreds;
    private URI crowdServer;
    private PoolingHttpClientConnectionManager cm;
    
    RestClient(CrowdPluginConfiguration config) throws URISyntaxException {
        crowdServer = new URI(config.getCrowdServerUrl()).resolve("rest/usermanagement/1/");

        crowdCreds = new UsernamePasswordCredentials(config.getApplicationName(), config.getApplicationPassword());

        // configure the http client
        RequestConfig.Builder reqConfigBuilder = RequestConfig.custom()
                .setAuthenticationEnabled(true)
                .setConnectTimeout(config.getHttpTimeout())
                .setSocketTimeout(config.getHttpTimeout());

        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(config.getHttpMaxConnections());
        cm.setDefaultMaxPerRoute(config.getHttpMaxConnections());

        // proxy settings
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        if (StringUtils.isNotBlank(config.getHttpProxyHost()) && config.getHttpProxyPort() > 0) {
            HttpHost proxy = new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort());
            reqConfigBuilder.setProxy(proxy);

            if (config.getHttpProxyUsername() != null && config.getHttpProxyPassword() != null) {
                credsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(config.getHttpProxyUsername(), config.getHttpProxyPassword()));
            }
        }

        RequestConfig reqConfig = reqConfigBuilder.build();
        HttpClientBuilder hcBuilder = HttpClients.custom()
                .setMaxConnPerRoute(config.getHttpMaxConnections())
                .setMaxConnTotal(config.getHttpMaxConnections())
                .setConnectionManager(cm)
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(reqConfig);


        client = hcBuilder.build();

        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP Client config");
            LOG.debug(config.getCrowdServerUrl());
            LOG.debug("PROPERTY_THREADPOOL_SIZE:" + cm.getMaxTotal());
            LOG.debug("PROPERTY_READ_TIMEOUT:" + reqConfig.getSocketTimeout());
            LOG.debug("PROPERTY_CONNECT_TIMEOUT:" + reqConfig.getConnectTimeout());
            if (reqConfig.getProxy() != null) {
                LOG.debug("PROPERTY_PROXY_URI:" + reqConfig.getProxy().toString());
            }
            LOG.debug("Crowd application name:" + config.getApplicationName());
        }
    }
    
    

    @Override
    protected void finalize() throws Throwable {
        cm.close();
        super.finalize();
    }



    /**
     * Authenticates a user with crowd. If authentication failed, raises a <code>RestException</code>
     * 
     * @param username
     * @param password
     * @return session token
     * @throws RestException
     */
    public void authenticate(String username, String password) throws RestException {
        HttpClientContext hc = HttpClientContext.create();
        HttpPost post = new HttpPost(crowdServer.resolve("authentication?username=" + urlEncode(username)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("authentication attempt for '{}'", username);
            LOG.debug(post.getURI().toString());
        }

        AuthenticatePost creds = new AuthenticatePost();
        creds.value = password;
        try {
            acceptXmlResponse(post);
            StringWriter writer = new StringWriter();
            JAXB.marshal(creds, writer);
            post.setEntity(EntityBuilder.create()
                    .setText(writer.toString())
                    .setContentType(ContentType.APPLICATION_XML)
                    .setContentEncoding(UTF8)
                    .build());

            enablePreemptiveAuth(post, hc);
            HttpResponse response = client.execute(post);
            
            switch (response.getStatusLine().getStatusCode()) {
              case HttpURLConnection.HTTP_OK:
                return;
              
              case HttpURLConnection.HTTP_BAD_REQUEST:
                throw createRestException(response);
              
              default:
                handleError(createRestException(response));
            }

        } catch (IOException | AuthenticationException ioe) {
            handleError(ioe);
        } finally {
            post.releaseConnection();
        }
    }


    /**
     * Retrieves the groups that the user is a nested member of
     * 
     * @param username
     * @return a set of roles (as strings)
     * @throws RestException
     */
    public Set<String> getNestedGroups(String username) throws RestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNestedGroups({})", username);
        }

        HttpClientContext hc = HttpClientContext.create();
        int maxResults = 100;
        int startIndex = 0;
        StringBuilder request = new StringBuilder("user/group/nested?username=").append(urlEncode(username))
                .append("&max-results=").append(maxResults)
                .append("&start-index=");

        return getGroupsFromCrowdLoop(hc, request, startIndex, maxResults);
    }


    /**
     * Retrieves cookie configurations
     * 
     * @return a <code>ConfigCookieGetResponse</code>
     * @throws RestException
     */
    public ConfigCookieGetResponse getCookieConfig() throws RestException {
        HttpClientContext hc = HttpClientContext.create();
        HttpGet get = new HttpGet(crowdServer.resolve("config/cookie"));

        if (LOG.isDebugEnabled()) {
            LOG.debug("ConfigCookieGetResponse getCookieConfig()");
            LOG.debug(get.getURI().toString());
        }

        ConfigCookieGetResponse configCookie = null;
        try {
            enablePreemptiveAuth(acceptXmlResponse(get), hc);
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() != 200) {
                handleError(createRestException(response));
            }
            configCookie = unmarshal(response, ConfigCookieGetResponse.class);

        } catch (IOException | JAXBException | AuthenticationException ioe) {
            handleError(ioe);
        } finally {
            get.releaseConnection();
        }

        return Objects.requireNonNull(configCookie);
    }


    /**
     * @param userid
     * @return a <code>org.sonatype.security.usermanagement.User</code> from Crowd by a userid
     * @throws RestException
     */
    public User getUser(String userid) throws RestException {
        HttpClientContext hc = HttpClientContext.create();
        HttpGet get = new HttpGet(crowdServer.resolve("user?username=" + urlEncode(userid)));

        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser({})", userid);
            LOG.debug(get.getURI().toString());
        }

        UserResponse user = null;
        try {
            enablePreemptiveAuth(acceptXmlResponse(get), hc);
            HttpResponse response = client.execute(get);
            
            switch(response.getStatusLine().getStatusCode()) {
                case HttpURLConnection.HTTP_OK:
                    break;
                  
                  case HttpURLConnection.HTTP_NOT_FOUND:
                    throw createRestException(response);
                  
                  default:
                    handleError(createRestException(response));
            }

            user = unmarshal(response, UserResponse.class);

        } catch (IOException | JAXBException | AuthenticationException ioe) {
            handleError(ioe);
        } finally {
            get.releaseConnection();
        }

        return Objects.requireNonNull(convertUser(user));
    }



    /**
     * Returns user list based on userid
     * @param userId
     * @param email
     * @param filterGroups
     * @return
     * @throws RestException
     * @throws UnsupportedEncodingException
     */
    public Set<User> searchUsers(String userId) throws RestException {
        LOG.debug("searchUsers({})", userId);

        HttpClientContext hc = HttpClientContext.create();
        int maxResults = 1000;

        if (StringUtils.isNotEmpty(userId)) {
            StringBuilder request = new StringBuilder("search?entity-type=user&max-results=").append(maxResults).append("&restriction=");

            StringBuilder searchQuery = new StringBuilder("active=true");
            searchQuery.append(" AND name=\"").append(userId.trim()).append("*\"");

            request.append(urlEncode(searchQuery.toString())).append("&start-index=");


            int startIndex = 0;
            Set<User> result = new HashSet<>();
            try {
                while (true) {
                    HttpGet get = enablePreemptiveAuth(acceptXmlResponse(new HttpGet(crowdServer.resolve(request.toString() + startIndex))), hc);
                    SearchUserGetResponse users = null;

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(get.getURI().toString());
                    }

                    try {
                        HttpResponse response = client.execute(get);
                        if (response.getStatusLine().getStatusCode() != 200) {
                            handleError(createRestException(response));
                        }
                        users = unmarshal(response, SearchUserGetResponse.class);
                    } finally {
                        get.releaseConnection();
                    }

                    if (users != null && users.user != null) {
                        for (UserResponse user : users.user) {
                            result.add(getUser(user.name));
                        }

                        if (users.user.size() != maxResults) {
                            break;
                        }

                        startIndex += maxResults;

                    } else {
                        break;
                    }
                }

            } catch (IOException | JAXBException | AuthenticationException ioe) {
                handleError(ioe);
            }


            return result;
        }

        return Collections.emptySet();
    }


    /**
     * 
     * @return all the crowd groups
     * @throws RestException
     */
    public Set<Role> getAllGroups() throws RestException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllGroups()");
        }

        HttpClientContext hc = HttpClientContext.create();
        int maxResults = 1000;
        int startIndex = 0;
        Set<Role> results = new HashSet<>();
        StringBuilder request = new StringBuilder("search?entity-type=group&expand=group&restriction=active%3dtrue")
        .append("&max-results=").append(maxResults)
        .append("&start-index=");

        Set<String> roleIds = getGroupsFromCrowdLoop(hc, request, startIndex, maxResults);
        for (String roleId : roleIds) {
            results.add(new Role(roleId, roleId, "", "", true, null, null));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("returning %d groups as Nexus Role objects", results.size()));
        }

        return results;
    }





    private Set<String> getGroupsFromCrowdLoop(HttpClientContext hc, StringBuilder request, int start, int maxResults) throws RestException {
        Set<String> results = new HashSet<>();
        try {
            int startIndex = start;
            while (true) {
                HttpGet get = enablePreemptiveAuth(acceptXmlResponse(new HttpGet(crowdServer.resolve(request.toString() + startIndex))), hc);
                GroupsResponse groups = null;

                if (LOG.isDebugEnabled()) {
                    LOG.debug(get.getURI().toString());
                }

                try {
                    HttpResponse response = client.execute(get);
                    
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpURLConnection.HTTP_OK:
                            break;
                        
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            throw createRestException(response);
                        
                        default:
                            handleError(createRestException(response));
                    }

                    groups = unmarshal(response, GroupsResponse.class);
                    
                } finally {
                    get.releaseConnection();
                }

                if (groups != null && groups.group != null) {
                    for (GroupResponse group : groups.group) {
                        results.add(group.name);
                    }

                    if (groups.group.size() != maxResults) {
                        break;
                    }
                    
                    startIndex += maxResults;

                } else {
                    break;
                }
            }

        } catch (IOException | JAXBException | AuthenticationException ioe) {
            handleError(ioe);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("returning %d groups", results.size()));
        }

        return results;
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, UTF8);
        } catch (UnsupportedEncodingException uee) {
            LOG.error("UTF-8 not supported ?", uee);
            return str;
        }
    }


    private static User convertUser(UserResponse in) {
        User user = new User();
        user.setUserId(in.name);
        user.setFirstName(in.firstName);
        user.setLastName(in.lastName);
        user.setEmailAddress(in.email);
        user.setStatus(in.active ? UserStatus.active : UserStatus.disabled);
        return user;
    }

    private static <T extends HttpRequestBase> T acceptXmlResponse(T method) {
        method.addHeader("Accept", MediaType.APPLICATION_XML);
        method.addHeader("Accept-Charset", UTF8);
        return method;
    }

    private <T extends HttpRequestBase> T enablePreemptiveAuth(T method, HttpClientContext hcc) throws AuthenticationException {
        HttpClientContext localContext = HttpClientContext.adapt(hcc);
        method.addHeader(new BasicScheme().authenticate(crowdCreds, method, localContext));
        return method;
    }

    private static <T> T unmarshal(HttpResponse response, Class<T> type) throws JAXBException, IOException {
        JAXBContext jaxbC = JAXBContext.newInstance(type);
        Unmarshaller um = jaxbC.createUnmarshaller();
        um.setEventHandler(new DefaultValidationEventHandler());
        return um.unmarshal(new StreamSource(response.getEntity().getContent()), type).getValue();
    }

    private static RestException createRestException(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        String statusText = statusLine.getReasonPhrase();
        String body = null;
        if (response.getEntity() != null) {
            try {
              body = EntityUtils.toString(response.getEntity(), UTF8);
            } catch (Exception e) {
              LOG.debug("Problem occured while reading a HTTP response", e);
            }
        }

        StringBuilder strBuf = new StringBuilder();
        strBuf.append("Crowd returned HTTP error code:").append(status);
        strBuf.append(" - ").append(statusText);
        if (StringUtils.isNotBlank(body)) {
            strBuf.append("\n").append(body);
        }

        return new RestException(strBuf.toString());
    }

    private static void handleError(Exception e) throws RestException {
        LOG.error("Error occured while consuming Crowd REST service", e);
        throw new RestException(e.getMessage());
    }
}