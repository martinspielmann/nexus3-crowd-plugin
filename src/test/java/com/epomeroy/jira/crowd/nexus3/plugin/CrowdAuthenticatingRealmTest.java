package com.epomeroy.jira.crowd.nexus3.plugin;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.epomeroy.jira.crowd.nexus3.plugin.internal.CachingNexusCrowdClient;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.CrowdProperties;

public class CrowdAuthenticatingRealmTest {

    private CrowdAuthenticatingRealm r;
    private CachingNexusCrowdClient mockedClient;
    private CrowdProperties mockedProperties;

    @Before
    public void setupTest() {
        mockedClient = Mockito.mock(CachingNexusCrowdClient.class);
        mockedProperties = Mockito.mock(CrowdProperties.class);
        r = new CrowdAuthenticatingRealm(mockedClient, mockedProperties);
    }

    @Test
    public void testGetName() {
        Assert.assertEquals(CrowdAuthenticatingRealm.NAME, r.getName());
    }

    @Test
    public void testDoGetAuthorizationInfoPrincipalCollection() {
        PrincipalCollection principals = new SimplePrincipalCollection("Test1", CrowdAuthenticatingRealm.NAME);
        Mockito.when(mockedClient.findRolesByUser("Test1")).thenReturn(fakeAuths());
        AuthorizationInfo info = r.doGetAuthorizationInfo(principals);
        Assert.assertEquals(2, info.getRoles().size());
        Assert.assertTrue(info.getRoles().contains("role1"));
        Assert.assertTrue(info.getRoles().contains("role2"));
    }

    private Set<String> fakeAuths() {
        Set<String> auths = new HashSet<>();
        auths.add("role1");
        auths.add("role2");
        return auths;
    }

    @Test
    public void testDoGetAuthenticationInfoNoCrowd() {
        AuthenticationToken token = new UsernamePasswordToken("u1", new char[] { 'p', '1' });
        AuthenticationInfo info = r.doGetAuthenticationInfo(token);
        Assert.assertNull(info);
    }

    @Test
    public void testDoGetAuthenticationInfoWithCrowdOK() {
        UsernamePasswordToken token = new UsernamePasswordToken("u1", new char[] { 'p', '1' });
        Mockito.when(mockedClient.authenticate(token)).thenReturn(true);
        AuthenticationInfo info = r.doGetAuthenticationInfo(token);
        Assert.assertNotNull(info);
    }

}
