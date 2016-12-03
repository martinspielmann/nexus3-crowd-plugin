package com.pingunaut.nexus3.crowd.plugin.internal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.pingunaut.nexus3.crowd.plugin.internal.entity.CachedToken;
import org.junit.Assert;
import org.junit.Test;

public class CacheProviderTest {

	CacheProvider p = new CacheProvider();

	@Test
	public void testPutToken() {
		p.putToken("foo", mockedCachedToken());
		Assert.assertArrayEquals(new byte[]{1,2,3}, p.getToken("foo").get().hash);
		Assert.assertArrayEquals(new byte[]{4,5,6}, p.getToken("foo").get().salt);
	}

	@Test
	public void testGetToken() {
		p.putToken("foo", mockedCachedToken());
		Assert.assertArrayEquals(new byte[]{1,2,3}, p.getToken("foo").get().hash);
		Assert.assertArrayEquals(new byte[]{4,5,6}, p.getToken("foo").get().salt);
	}

	@Test
	public void testGetTokenEmpty() {
		p.putToken("foo",  mockedCachedToken());
		Assert.assertEquals(Optional.empty(), p.getToken("foo2"));
	}

	@Test
	public void testGetGroups() {
		Set<String> set = new HashSet<>();
		set.add("bar");
		p.putGroups("foo", set);
		Assert.assertEquals("bar", p.getGroups("foo").get().toArray()[0]);	}

	@Test
	public void testPutGroups() {
		Set<String> set = new HashSet<>();
		set.add("bar");
		p.putGroups("foo", set);
		Assert.assertEquals("bar", p.getGroups("foo").get().toArray()[0]);
	}

	CachedToken mockedCachedToken(){
		return new CachedToken(new byte[]{1,2,3}, new byte[]{4,5,6});
	}

}
