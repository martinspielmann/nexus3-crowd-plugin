package com.pingunaut.nexus3.crowd.plugin.internal;

import com.pingunaut.nexus3.crowd.plugin.CrowdAuthenticatingRealm;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CrowdClientTest {

    // mock creation
    CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);

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


}
