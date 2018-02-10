package com.pingunaut.nexus3.crowd.plugin.internal;

import com.google.inject.Inject;
import com.pingunaut.nexus3.crowd.plugin.CrowdAuthenticatingRealm;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.mapper.CrowdMapper;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.spi.CharsetProvider;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CachingNexusCrowdClientTest {

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
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
        when(mockedClient.normalizeCrowdServerUri("sdfadsfa/")).thenCallRealMethod();
        String uri = mockedClient.normalizeCrowdServerUri("sdfadsfa/");
        Assert.assertEquals("sdfadsfa", uri);
    }

    @Test
    public void testNormalizeCrowdServerUriThatsOkAlready() {
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
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
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
        char[] input = new char[]{1,2,3};
        when(mockedClient.createCachedToken(input)).thenCallRealMethod();
        CachedToken t = mockedClient.createCachedToken(input);
        Assert.assertEquals(32, t.hash.length);
        Assert.assertEquals(16, t.salt.length);
    }

    @Test
    public void testAuthenticateNoCache()  {
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        when(mockedClient.authenticate(token)).thenCallRealMethod();
        when(mockedClient.executeQuery(any(),any())).thenReturn("foo");
        when(mockedClient.getServerUriString()).thenReturn("bar");
        boolean auth = mockedClient.authenticate(token);
        Assert.assertTrue(auth);
    }

    @Test
    public void testAuthenticateNoCacheAuthFail()  {
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        when(mockedClient.authenticate(token)).thenCallRealMethod();
        when(mockedClient.executeQuery(any(),any())).thenReturn(null);
        when(mockedClient.getServerUriString()).thenReturn("bar");
        boolean auth = mockedClient.authenticate(token);
        Assert.assertFalse(auth);
    }

    @Test
    public void testAuthenticateCacheEnabled()  {
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);

        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        when(mockedClient.authenticate(token)).thenCallRealMethod();
        when(mockedClient.executeQuery(any(),any())).thenReturn("foo");
        when(mockedClient.getServerUriString()).thenReturn("bar");
        CacheProvider cache = mock(CacheProvider.class);
        when(mockedClient.getCache()).thenReturn(cache);
        when(mockedClient.isAuthCacheEnabled()).thenReturn(Boolean.TRUE);
        boolean auth = mockedClient.authenticate(token);

        Assert.assertTrue(auth);
        verify(cache, times(1)).putToken(any(),any());
    }

    @Test
    public void testAuthenticateCacheEnabledCacheHit()  {
        CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);

        UsernamePasswordToken token = new UsernamePasswordToken("user123", "password123");
        when(mockedClient.authenticate(token)).thenCallRealMethod();
        when(mockedClient.executeQuery(any(),any())).thenReturn("foo");
        when(mockedClient.getServerUriString()).thenReturn("bar");
        CacheProvider cache = mock(CacheProvider.class);
        when(mockedClient.getCache()).thenReturn(cache);
        when(mockedClient.authenticateFromCache(any())).thenReturn(true);
        when(mockedClient.isAuthCacheEnabled()).thenReturn(Boolean.TRUE);
        boolean auth = mockedClient.authenticate(token);

        Assert.assertTrue(auth);
        verify(cache, times(0)).putToken(any(),any());
    }

    @Test
    public void testGetServerUriString(){
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        Assert.assertEquals("http://foobar", client.getServerUriString());
    }

    @Test
    public void testIsAuthCacheEnabled(){
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        Assert.assertTrue(client.isAuthCacheEnabled());
    }

    @Test
    public void testGetCache() {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        Assert.assertEquals(cache, client.getCache());
    }

    @Test
    public void testRestUri() {
        CrowdProperties props = mock(CrowdProperties.class);
        CacheProvider cache = mock(CacheProvider.class);
        when(props.isCacheAuthenticationEnabled()).thenReturn(Boolean.TRUE);
        when(props.getServerUrl()).thenReturn("http://foobar/");
        when(props.getApplicationName()).thenReturn("app");
        when(props.getApplicationPassword()).thenReturn("passw");
        CachingNexusCrowdClient client = new CachingNexusCrowdClient(props, cache);
        Assert.assertEquals("http://foobar/rest/usermanagement/1/blub", client.restUri("blub"));
    }
}
