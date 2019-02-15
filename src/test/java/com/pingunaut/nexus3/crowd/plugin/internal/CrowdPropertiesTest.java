package com.pingunaut.nexus3.crowd.plugin.internal;

import org.junit.Assert;
import org.junit.Test;

public class CrowdPropertiesTest {

    @Test
    public void testGetConnectTimeoutWithDefaults() {
        CrowdProperties crowdProperties = new CrowdProperties();
        Assert.assertEquals(15000, crowdProperties.getConnectTimeout());
    }

}