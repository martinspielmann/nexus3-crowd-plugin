/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class DefaultCrowdPluginConfiguration implements CrowdPluginConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(DefaultCrowdPluginConfiguration.class);

    private final String DEFAULT_HTTP_PROXY_PORT = "0";
    private final String DEFAULT_HTTP_MAX_CONNECTIONS = "20";
    private final String DEFAULT_HTTP_TIMEOUT = "5000"; // default is 5000 milliseconds
    private final String DEFAULT_CACHE_TTL = "3600"; // default is 3600 seconds

    private Properties crowdConfigProperties;

    public DefaultCrowdPluginConfiguration() throws FileNotFoundException, IOException {
        String karafEtc = System.getProperty("karaf.etc");
        if (StringUtils.isEmpty(karafEtc)) {
            throw new RuntimeException("cannot load karaf.etc property value");
        }

        File crowdConfigFile = new File(karafEtc, "crowd-plugin.properties");
        crowdConfigProperties = new Properties();
        crowdConfigProperties.load(new FileInputStream(crowdConfigFile));

        if (LOG.isTraceEnabled()) {
            LOG.trace("content of crowd plugin config file");
            crowdConfigProperties.forEach((k, v) -> LOG.trace(k + ": " + v)); 
        }
    }

    @Override
    public String getApplicationName() {
        String applicationName = crowdConfigProperties.getProperty("applicationName");
        if (StringUtils.isEmpty(applicationName)) {
            throw new RuntimeException("Crowd application name is missing for Crowd plugin");
        }
        return applicationName;
    }

    @Override
    public String getApplicationPassword() {
        String applicationPassword = crowdConfigProperties.getProperty("applicationPassword");
        if (StringUtils.isEmpty(applicationPassword)) {
            throw new RuntimeException("Crowd application password is missing for Crowd plugin");
        }
        return applicationPassword;
    }

    @Override
    public String getCrowdServerUrl() {
        String crowdServerUrl = crowdConfigProperties.getProperty("crowdServerUrl");
        if (StringUtils.isEmpty(crowdServerUrl)) {
            throw new RuntimeException("Crowd server URL is missing for Crowd plugin");
        }
        if (!crowdServerUrl.endsWith("/")) {
            crowdServerUrl += "/";
        }
        return crowdServerUrl;
    }

    @Override
    public int getCacheTTL() {
        String value = crowdConfigProperties.getProperty("cacheTTL", DEFAULT_CACHE_TTL);
        return Integer.parseInt(value);
    }

    @Override
    public int getHttpMaxConnections() {
        String value = crowdConfigProperties.getProperty("httpMaxConnections", DEFAULT_HTTP_MAX_CONNECTIONS);
        return Integer.parseInt(value);
    }

    @Override
    public String getHttpProxyHost() {
        return crowdConfigProperties.getProperty("httpProxyHost");
    }

    @Override
    public String getHttpProxyPassword() {
        return crowdConfigProperties.getProperty("httpProxyPassword");
    }

    @Override
    public int getHttpProxyPort() {
        String value = crowdConfigProperties.getProperty("httpProxyPort", DEFAULT_HTTP_PROXY_PORT);
        return Integer.parseInt(value);
    }

    @Override
    public String getHttpProxyUsername() {
        return crowdConfigProperties.getProperty("httpProxyUsername");
    }

    @Override
    public int getHttpTimeout() {
        String value = crowdConfigProperties.getProperty("httpTimeout", DEFAULT_HTTP_TIMEOUT);
        return Integer.parseInt(value);
    }

}
