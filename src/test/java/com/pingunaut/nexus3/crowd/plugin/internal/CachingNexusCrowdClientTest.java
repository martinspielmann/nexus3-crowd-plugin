package com.pingunaut.nexus3.crowd.plugin.internal;

import com.google.inject.Inject;
import com.pingunaut.nexus3.crowd.plugin.CrowdAuthenticatingRealm;
import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
