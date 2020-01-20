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
package com.epomeroy.jira.crowd.nexus3.plugin.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static int parseWithDefault(String s, int defaultValue) {
        return s != null && s.matches("-?\\d+") ? Integer.parseInt(s) : defaultValue;
    }

    private String readFile(String filePath) {
        if (filePath == null) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(Paths.get(filePath)),
                    StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            LOGGER.error("Could not read Application Password from environment", e);
        }

        return null;
    }

    public String getServerUrl() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String serverUrl = System.getenv("SERVER_URL");
        return serverUrl != null ? serverUrl : configuration.getProperty("crowd.server.url");
    }

    public String getApplicationName() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String applicationName = System.getenv("APPLICATION_USERNAME");
        return applicationName != null ? applicationName : configuration.getProperty("application.name");
    }

    public String getApplicationPassword() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        // Docker secrets are files in the /run/secrets folder and are passed as the full filename
        String applicationPassword = readFile(System.getenv("APPLICATION_PASSWORD"));
        return applicationPassword != null ? applicationPassword : configuration.getProperty("application.password");
    }

    public String getJiraUserGroup() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        // Docker secrets are files in the /run/secrets folder and are passed as the full filename
        String jiraUserGroup = readFile(System.getenv("JIRA_USER_GROUP"));
        return jiraUserGroup != null ? jiraUserGroup : configuration.getProperty("jira.user.group");
    }

    public int getConnectTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String connectionTimeout = System.getenv("CONNECTION_TIMEOUT");
        return connectionTimeout != null ? Integer.parseInt(connectionTimeout) : parseWithDefault(configuration.getProperty("timeout.connect"), DEFAULT_TIMEOUT);
    }

    public int getSocketTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String socketTimeout = System.getenv("SOCKET_TIMEOUT");
        return socketTimeout != null ? Integer.parseInt(socketTimeout) : parseWithDefault(configuration.getProperty("timeout.socket"), DEFAULT_TIMEOUT);
    }

    public int getConnectionRequestTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String connectionRequestTimeout = System.getenv("CONNECTION_REQUEST_TIMEOUT");
        return connectionRequestTimeout != null ? Integer.parseInt(connectionRequestTimeout) : parseWithDefault(configuration.getProperty("timeout.connectionrequest"), DEFAULT_TIMEOUT);
    }

    public boolean isCacheAuthenticationEnabled() {
        String enabled = configuration.getProperty("cache.authentication");
        return Boolean.valueOf(enabled);
    }

    public byte[] getBasicAuthorization() {
        StringBuilder sb = new StringBuilder();
        return sb.append(getApplicationName()).append(":").append(getApplicationPassword()).toString().getBytes(StandardCharsets.UTF_8);
    }
}
