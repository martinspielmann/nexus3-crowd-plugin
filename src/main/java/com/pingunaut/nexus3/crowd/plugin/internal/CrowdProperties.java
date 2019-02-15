/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 * <p>
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.pingunaut.nexus3.crowd.plugin.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Singleton
@Named
public class CrowdProperties {

    private static final String CONFIG_FILE = "crowd.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdProperties.class);

    private static final int DEFAULT_TIMEOUT = 15000;

    private Properties configuration;

    public CrowdProperties() {
        configuration = new Properties();
        try {
            Path p = Paths.get(System.getProperty("karaf.data"), "etc", CONFIG_FILE);
            if (!Files.exists(p)) {
                LOGGER.warn("DEPRECATION: Please place your crowd.properties  in the $data-dir/etc/ to be able to update without copy manual copy steps");
                p = Paths.get(".", "etc", CONFIG_FILE);
            }
            configuration.load(Files.newInputStream(p));
        } catch (IOException e) {
            LOGGER.error("Error reading crowd properties", e);
        }
    }

    public String getServerUrl() {
        return configuration.getProperty("crowd.server.url");
    }

    public String getApplicationName() {
        return configuration.getProperty("application.name");
    }

    public String getApplicationPassword() {
        return configuration.getProperty("application.password");
    }

    public int getConnectTimeout() {
        return parseWithDefault(configuration.getProperty("timeout.connect"), DEFAULT_TIMEOUT);
    }

    public int getSocketTimeout() {
        return parseWithDefault(configuration.getProperty("timeout.socket"), DEFAULT_TIMEOUT);
    }

    public int getConnectionRequestTimeout() {
        return parseWithDefault(configuration.getProperty("timeout.connectionrequest"), DEFAULT_TIMEOUT);
    }

    public boolean isCacheAuthenticationEnabled() {
        String enabled = configuration.getProperty("cache.authentication");
        return Boolean.valueOf(enabled);
    }

    private static int parseWithDefault(String s, int defaultValue) {
        return s != null && s.matches("-?\\d+") ? Integer.parseInt(s) : defaultValue;
    }
}
