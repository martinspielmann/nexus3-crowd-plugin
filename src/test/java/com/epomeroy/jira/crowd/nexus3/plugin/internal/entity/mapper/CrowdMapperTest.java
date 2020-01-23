package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.mapper;

import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import com.epomeroy.jira.crowd.nexus3.plugin.internal.CrowdUserManager;

/**
 * Tests for a {@link CrowdMapper}.
 *
 * @author Zhenlei Huang
 */
public class CrowdMapperTest {

    @Test
    public void testToAuthToken() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"session\": {\"name\": \"JSESSIONID\", \"value\": \"403EBD29676819FAC3AC5BC217998DD8\"}, loginInfo: {\"failedLoginCount\": 42,\"loginCount\": 9000}}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);

        assertEquals("403EBD29676819FAC3AC5BC217998DD8", CrowdMapper.toAuthToken(response));
    }

    @Test
    public void testToAuthTokenWithInalidUser() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"reason\":\"INVALID_USER_AUTHENTICATION\",\"message\":\"Account with name <anonymous> failed to authenticate: User <anonymous> does not exist\"}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);

        when(statusLine.getStatusCode()).thenReturn(400);

        assertNull(CrowdMapper.toAuthToken(response));
    }

    @Test
    public void testToRoleStrings() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"name\":\"jane\", \"groups\": {\"size\": 3,\"items\": [{\"name\":\"bitbucket-users\"},{\"name\":\"jenkins-users\"}, {\"name\":\"nx-users\"}]}}", ContentType.APPLICATION_JSON);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);

        Set<String> roleStrings = CrowdMapper.toRoleStrings(response);
        assertThat(roleStrings, hasSize(3));
        assertThat(roleStrings, hasItems("bitbucket-users", "jenkins-users", "nx-users"));
    }

    @Test
    public void testToRoleStringsWithUserNotFound() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"reason\":\"USER_NOT_FOUND\",\"message\":\"User <anonymous> does not exist\"}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(404);

        Set<String> roleStrings = CrowdMapper.toRoleStrings(response);
        assertThat(roleStrings, empty());
    }

    @Test
    public void testToUser() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"name\":\"csagan\", \"emailAddress\": \"csagan@seti.org\", \"displayName\": \"Carl Sagan\", \"active\": true, \"timeZone\": \"America/New York\", \"groups\": {\"size\": 3,\"items\": [{\"name\":\"bitbucket-users\"},{\"name\":\"jenkins-users\"}, {\"name\":\"nx-users\"}]}}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);

        User user = CrowdMapper.toUser(response);

        assertNotNull(user);
        assertEquals("csagan", user.getUserId());
        assertEquals("Carl", user.getFirstName());
        assertEquals("Sagan", user.getLastName());
        assertEquals("csagan@seti.org", user.getEmailAddress());
        assertEquals(UserStatus.active, user.getStatus());
        Assert.assertEquals(CrowdUserManager.SOURCE, user.getSource());
    }

    @Test
    public void testToUserWithUserNotFound() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{\"reason\":\"USER_NOT_FOUND\",\"message\":\"User <anonymous> does not exist\"}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(404);

        assertNull(CrowdMapper.toUser(response));
    }

//    @Test
//    public void testToRole() {
//        HttpResponse response = mock(HttpResponse.class);
//        StatusLine statusLine = mock(StatusLine.class);
//        HttpEntity httpEntity = new StringEntity("{\"name\": \"nx-admin\"}", ContentType.APPLICATION_JSON);
//
//        when(response.getStatusLine()).thenReturn(statusLine);
//        when(response.getEntity()).thenReturn(httpEntity);
//        when(statusLine.getStatusCode()).thenReturn(200);
//
//        Role role = CrowdMapper.toRole(response);
//
//        assertNotNull(role);
//        assertEquals("nx-admin", role.getRoleId());
//        assertEquals("nx-admin", role.getName());
//        assertEquals("nx-admin", role.getDescription());
//        assertEquals(CrowdUserManager.SOURCE, role.getSource());
//    }

//    @Test
//    public void testToRoleWithRoleNotFound() {
//        HttpResponse response = mock(HttpResponse.class);
//        StatusLine statusLine = mock(StatusLine.class);
//        HttpEntity httpEntity = new StringEntity("{\"reason\":\"GROUP_NOT_FOUND\",\"message\":\"Group <anonymous> does not exist\"}", ContentType.APPLICATION_JSON);
//
//        when(response.getStatusLine()).thenReturn(statusLine);
//        when(response.getEntity()).thenReturn(httpEntity);
//        when(statusLine.getStatusCode()).thenReturn(404);
//
//        assertNull(CrowdMapper.toRole(response));
//    }

    @Test
    public void testToUsers() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{values:[{\"name\":\"csagan\", \"emailAddress\": \"csagan@seti.org\", \"displayName\": \"Carl Sagan\", \"active\": true, \"timeZone\": \"America/New York\", \"groups\": {\"size\": 3,\"items\": [{\"name\":\"bitbucket-users\"},{\"name\":\"jenkins-users\"}, {\"name\":\"nx-users\"}]}}, "
                                                 + "{\"name\":\"mcurie\", \"emailAddress\": \"mcurie@home.cern\", \"displayName\": \"Marie Curie\", \"active\": true, \"timeZone\": \"Europe/Geneva\", \"groups\": {\"size\": 3,\"items\": [{\"name\":\"bitbucket-admins\"},{\"name\":\"jenkins-admins\"}, {\"name\":\"nx-users\"}]}}]}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);

        Set<User> users = CrowdMapper.toUsers(response);

        assertThat(users, hasSize(2));

        User u1 = new User();
        u1.setUserId("csagan");
        u1.setSource(CrowdUserManager.SOURCE);

        User u2 = new User();
        u2.setUserId("mcurie");
        u2.setSource(CrowdUserManager.SOURCE);

        assertThat(users, hasItems(u1, u2));
    }

    @Test
    public void testToUsersUnauthorized() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("Application failed to authenticate", ContentType.TEXT_PLAIN);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(401);

        assertThat(CrowdMapper.toUsers(response), empty());
    }

    @Test
    public void testToRoles() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("{groups: {\"size\": 2,\"items\": [{\"name\": \"jira-admin\"}, {\"name\":\"jira-user\"}]}}", ContentType.APPLICATION_JSON);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(200);

        Set<String> roles = CrowdMapper.toRoleStrings(response);

        assertThat(roles, hasSize(2));

        String r1 = new String("jira-admin");
        String r2 = new String("jira-user");

        assertThat(roles, hasItems(r1, r2));
    }

    @Test
    public void testToRolesUnauthorized() {
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = new StringEntity("Application failed to authenticate", ContentType.TEXT_PLAIN);

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);
        when(statusLine.getStatusCode()).thenReturn(401);

        assertThat(CrowdMapper.toRoleStrings(response), empty());
    }
}
