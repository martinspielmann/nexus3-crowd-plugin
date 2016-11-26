package com.pingunaut.nexus3.crowd.plugin.internal;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CacheProviderTest {

	CacheProvider p = new CacheProvider();

	@Test
	public void testPutToken() {
		p.putToken("foo", "bar");
		Assert.assertEquals("bar", p.getToken("foo").get());
	}

	@Test
	public void testGetToken() {
		p.putToken("foo", "bar");
		Assert.assertEquals("bar", p.getToken("foo").get());
	}

	@Test
	public void testGetTokenEmpty() {
		p.putToken("foo", "bar");
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

}
