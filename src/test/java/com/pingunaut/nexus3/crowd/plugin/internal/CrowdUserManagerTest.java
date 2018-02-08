package com.pingunaut.nexus3.crowd.plugin.internal;

import com.pingunaut.nexus3.crowd.plugin.CrowdAuthenticatingRealm;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CrowdUserManagerTest {

	// mock creation
	CachingNexusCrowdClient mockedClient = mock(CachingNexusCrowdClient.class);
	CrowdUserManager m = new CrowdUserManager(mockedClient);

	@Test
	public void testGetAuthenticationRealmName() {
		Assert.assertEquals(CrowdAuthenticatingRealm.NAME, m.getAuthenticationRealmName());
	}

	@Test
	public void testGetSource() {
		Assert.assertEquals("Crowd", m.getSource());
	}

	@Test
	public void testListUsers() {
		when(mockedClient.findUsers()).thenReturn(mockedUsers());
		Assert.assertEquals(2, m.listUsers().size());
	}

	private Set<User> mockedUsers() {
		Set<User> u = new HashSet<>();
		User u1 = new User();
		User u2 = new User();
		u1.setUserId("1");
		u2.setUserId("2");
		u.add(u1);
		u.add(u2);
		return u;
	}

	@Test
	public void testListUserIds() {
		when(mockedClient.findAllUsernames())
				.thenReturn(mockedUsers().stream().map(User::getUserId).collect(Collectors.toSet()));
		Assert.assertEquals(2, m.listUserIds().size());
	}

	@Test
	public void testSearchUsers() {
		UserSearchCriteria usc = new UserSearchCriteria("1");
		when(mockedClient.findUserByCriteria(usc)).thenReturn(mockedUsers());
		Assert.assertEquals(2, m.searchUsers(usc).size());
	}

	@Test
	public void testGetUser() throws UserNotFoundException {
		when(mockedClient.findUserByUsername("1")).thenReturn(mockedUsers().iterator().next());
		User u = mockedUsers().iterator().next();
		u.setSource("Crowd");
		Assert.assertEquals(u, m.getUser("1"));
	}

	@Test(expected = UserNotFoundException.class)
	public void testGetUserWithNull() throws UserNotFoundException {
		when(mockedClient.findUserByUsername("1")).thenReturn(null);
        m.getUser("1");
	}

}
