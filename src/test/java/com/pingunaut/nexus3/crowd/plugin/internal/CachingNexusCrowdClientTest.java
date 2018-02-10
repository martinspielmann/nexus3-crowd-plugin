package com.pingunaut.nexus3.crowd.plugin.internal;

import com.google.inject.Inject;
import com.pingunaut.nexus3.crowd.plugin.CrowdAuthenticatingRealm;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.mapper.CrowdMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CachingNexusCrowdClientTest {

    CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);

    @Test
    public void testConstructor() {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        Assert.assertNotNull(client.getClient());
    }

    @Test
    public void testNormalizeCrowdServerUri() {
        when(mockedClient.normalizeCrowdServerUri("sdfadsfa/")).thenCallRealMethod();
        String uri = mockedClient.normalizeCrowdServerUri("sdfadsfa/");
        Assert.assertEquals("sdfadsfa", uri);
    }

    @Test
    public void testNormalizeCrowdServerUriThatsOkAlready() {
        when(mockedClient.normalizeCrowdServerUri("sdfadsfa")).thenCallRealMethod();
        String uri = mockedClient.normalizeCrowdServerUri("sdfadsfa");
        Assert.assertEquals("sdfadsfa", uri);
    }

    @Test
    public void authenticateFromCacheFooToken() {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        UsernamePasswordToken token = new UsernamePasswordToken("u", "p");
        Optional<CachedToken> ot = Optional.of(new CachedToken(new byte[]{1,2,3}, new byte[]{1,2,3}));
        when(cache.getToken("u")).thenReturn(ot);
        boolean auth = client.authenticateFromCache(token);
        Assert.assertEquals(false, auth);
    }

    @Test
    public void authenticateFromCacheCorrectToken() {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);

        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        CachedToken ct = client.createCachedToken(token.getPassword());
        Optional<CachedToken> ot = Optional.of(ct);
        when(cache.getToken("user123")).thenReturn(ot);
        UsernamePasswordToken token2 = new UsernamePasswordToken("user123", "password123");
        boolean auth = client.authenticateFromCache(token2);
        Assert.assertEquals(true, auth);
    }

    @Test
    public void testCreateCachedToken()  {
        char[] input = new char[]{1,2,3};
        when(mockedClient.createCachedToken(input)).thenCallRealMethod();
        CachedToken t = mockedClient.createCachedToken(input);
        Assert.assertEquals(32, t.hash.length);
        Assert.assertEquals(16, t.salt.length);
    }

    @Test
    public void testAuthenticateNoCache()  {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.FALSE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        try {
            when(httpClient.execute(any(HttpHost.class), any(), any(ResponseHandler.class))).thenReturn("foo");
        }catch (IOException e){

        }
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache){
            @Override
            protected CloseableHttpClient getClient() {
                return httpClient;
            }
        };
        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        boolean auth = client.authenticate(token);

        Assert.assertTrue(auth);
    }

    @Test
    public void testAuthenticateNoCacheAuthFail()  {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.FALSE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        try {
            when(httpClient.execute(any(HttpHost.class), any(), any(ResponseHandler.class))).thenReturn(null);
        }catch (IOException e){

        }
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache){
            @Override
            protected CloseableHttpClient getClient() {
                return httpClient;
            }
        };
        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        boolean auth = client.authenticate(token);

        Assert.assertFalse(auth);
    }

    @Test
    public void testAuthenticateCacheEnabled()  {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(cache.getToken(any())).thenReturn(Optional.of(new CachedToken(new byte[]{1,2,3}, new byte[]{1,2,3})));
        try {
            when(httpClient.execute(any(HttpHost.class), any(), any(ResponseHandler.class))).thenReturn("foo");
        }catch (IOException e){

        }
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache){
            @Override
            protected CloseableHttpClient getClient() {
                return httpClient;
            }
        };
        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        boolean auth = client.authenticate(token);

        Assert.assertTrue(auth);
        verify(cache, times(1)).putToken(any(),any());
    }

    /*
    public boolean authenticate(UsernamePasswordToken token) {
        // check if token is cached
        if(authCacheEnabled && authenticateFromCache(token)){
            return true;
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
    }*/
}
