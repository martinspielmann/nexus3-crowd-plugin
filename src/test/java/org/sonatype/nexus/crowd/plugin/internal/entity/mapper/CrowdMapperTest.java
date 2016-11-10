package org.sonatype.nexus.crowd.plugin.internal.entity.mapper;

import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import org.sonatype.nexus.crowd.plugin.internal.CrowdUserManager;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import org.junit.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"user\",\"token\":\"sometoken\"}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(201);

		assertEquals("sometoken", CrowdMapper.toAuthToken(response));
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
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"group\",\"groups\":[{\"name\":\"bitbucket-users\"},{\"name\":\"jenkins-users\"},{\"name\":\"nx-users\"}]}", ContentType.APPLICATION_JSON);

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
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"attributes\",\"name\":\"greg\",\"active\":true,\"first-name\":\"Greg\",\"last-name\":\"Dunn\",\"display-name\":\"Greg Dunn\",\"email\":\"greg@example.com\"}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(200);

		User user = CrowdMapper.toUser(response);

		assertNotNull(user);
		assertEquals("greg", user.getUserId());
		// TODO fix parsing firstname / lastname
		//assertEquals("Greg", user.getFirstName());
		//assertEquals("Dunn", user.getLastName());
		assertEquals("greg@example.com", user.getEmailAddress());
		assertEquals(UserStatus.active, user.getStatus());
		assertEquals(CrowdUserManager.SOURCE, user.getSource());
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

	@Test
	public void testToRole() {
		HttpResponse response = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"attributes\",\"name\":\"nx-admin\",\"description\":\"Nexus repo administrator group\",\"type\":\"GROUP\",\"active\":true}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(200);

		Role role = CrowdMapper.toRole(response);

		assertNotNull(role);
		assertEquals("nx-admin", role.getRoleId());
		assertEquals("nx-admin", role.getName());
		assertEquals("Nexus repo administrator group", role.getDescription());
		assertEquals(CrowdUserManager.SOURCE, role.getSource());
	}

	@Test
	public void testToRoleWithRoleNotFound() {
		HttpResponse response = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		HttpEntity httpEntity = new StringEntity("{\"reason\":\"GROUP_NOT_FOUND\",\"message\":\"Group <anonymous> does not exist\"}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(404);

		assertNull(CrowdMapper.toRole(response));
	}

	@Test
	public void testToUsers() {
		HttpResponse response = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"user\",\"users\":[{\"name\":\"greg\",\"active\":true,\"first-name\":\"Greg\",\"last-name\":\"Dunn\",\"display-name\":\"Greg Dunn\",\"email\":\"greg@example.com\"},{\"name\":\"adam\",\"active\":false,\"first-name\":\"Adam\",\"last-name\":\"Ben\",\"display-name\":\"Adam Ben\",\"email\":\"adam@example.com\"}]}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(200);

		Set<User> users = CrowdMapper.toUsers(response);

		assertThat(users, hasSize(2));

		User u1 = new User();
		u1.setUserId("greg");
		u1.setSource(CrowdUserManager.SOURCE);

		User u2 = new User();
		u2.setUserId("adam");
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
		HttpEntity httpEntity = new StringEntity("{\"expand\":\"group\",\"groups\":[{\"name\":\"nx-admin\",\"type\":\"GROUP\",\"active\":true},{\"name\":\"nx-user\",\"type\":\"GROUP\",\"active\":false}]}", ContentType.APPLICATION_JSON);

		when(response.getStatusLine()).thenReturn(statusLine);
		when(response.getEntity()).thenReturn(httpEntity);
		when(statusLine.getStatusCode()).thenReturn(200);

		Set<Role> roles = CrowdMapper.toRoles(response);

		assertThat(roles, hasSize(2));

		Role r1 = new Role("nx-admin", "nx-admin", null, CrowdUserManager.SOURCE, true, null, null);
		Role r2 = new Role("nx-user", "nx-user", null, CrowdUserManager.SOURCE, true, null, null);

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

		assertThat(CrowdMapper.toRoles(response), empty());
	}
}
