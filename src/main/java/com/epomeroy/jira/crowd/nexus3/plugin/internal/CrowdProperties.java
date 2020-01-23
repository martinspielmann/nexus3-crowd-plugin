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

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;

import com.google.gson.Gson;

@Singleton
@Named
public class CrowdProperties {

    private static final String CONFIG_FILE = "crowd-properties.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdProperties.class);

    private static final int DEFAULT_TIMEOUT = 15000;

    private CrowdConfigurationFile configuration;

    public CrowdProperties() {
        try {
            Gson gson = new Gson();
            Path p = Paths.get(System.getProperty("karaf.data"), "etc", CONFIG_FILE);
            if (!Files.exists(p)) {
                LOGGER.warn("DEPRECATION: Please place your crowd.properties  in the $data-dir/etc/ to be able to update without copy manual copy steps");
                p = Paths.get(".", "etc", CONFIG_FILE);
            }
            configuration = gson.fromJson(new FileReader(p.toFile()), CrowdConfigurationFile.class);
        } catch (IOException e) {
            // Set a empty configuration for testing
            this.configuration = new CrowdConfigurationFile();
            LOGGER.error("Error reading crowd properties", e);
        }
    }

    private static int parseWithDefault(Integer i, int defaultValue) {
        return i != null ? i : defaultValue;
    }

    private static String parseWithDefault(String s, String defaultValue) {
        return s != null ? s : defaultValue;
    }

    public static Role getNexusRole(ImmutablePair<String, String> role) {
        String nexusRole = role.left;
        return new Role(nexusRole, nexusRole, nexusRole, CrowdUserManager.SOURCE, true, null, null);
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
        String serverUrl = StringUtils.isNotEmpty(System.getenv("SERVER_URL")) ? System.getenv("SERVER_URL") : configuration.getServerURL();
        if (StringUtils.isEmpty(serverUrl)) {
            LOGGER.error("SERVER_URL is not set");
            return "";
        }

        return serverUrl;
    }

    public String getApplicationName() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String applicationName = StringUtils.isNotEmpty(System.getenv("APPLICATION_USERNAME")) ? System.getenv("APPLICATION_USERNAME") : parseWithDefault(configuration.getAppUser(), "nexus");
        if (StringUtils.isEmpty(applicationName)) {
            LOGGER.error("APPLICATION_USERNAME is not set");
            return "";
        }

        return applicationName;
    }



    public String getApplicationPassword() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        // Docker secrets are files in the /run/secrets folder and are passed as the full filename
        String pw = readFile(System.getenv("APPLICATION_PASSWORD"));
        String applicationPassword = !StringUtils.isNotEmpty(pw) ? pw : readFile(configuration.getAppPass());
        if (StringUtils.isEmpty(applicationPassword)) {
            LOGGER.error("APPLICATION_PASSWORD is not set");
            return "";
        }

        return applicationPassword;
    }

    /**
     * Default group to filter user list
     *
     * @return Group all users must belong to in order to login and use nexus
     */
    public String getFilterGroup() {
        return configuration.getFilterGroup();
    }

    /**
     * Returns a list of pairs containing Nexus group and Jira group
     *
     * @return Nexus group is Left, Jira is Right
     */
    public List<ImmutablePair<String, String>> getRoleMapping() {
        ArrayList<ImmutablePair<String, String>> groups = new ArrayList<>();

        for (RoleMapping mapping : configuration.getRoleMapping()) {
            groups.add(new ImmutablePair<>(mapping.getNexusRole(), mapping.getJiraRole()));
        }

        return groups;
    }

    public String mapRole(String jiraRole) {
        for (ImmutablePair<String, String> mapping : this.getRoleMapping()) {
            if (mapping.right.equals(jiraRole)) {
                return mapping.left;
            }
        }

        return null;
    }

    public int getConnectTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String connectionTimeout = System.getenv("CONNECTION_TIMEOUT");
        return connectionTimeout != null ? Integer.parseInt(connectionTimeout) : parseWithDefault(configuration.getConnectTimeout(), DEFAULT_TIMEOUT);
    }

    public int getSocketTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String socketTimeout = System.getenv("SOCKET_TIMEOUT");
        return socketTimeout != null ? Integer.parseInt(socketTimeout) : parseWithDefault(configuration.getSocketTimeout(), DEFAULT_TIMEOUT);
    }

    public int getConnectionRequestTimeout() {
        // Read a docker env setting and try to use it, otherwise fall back to config file
        String connectionRequestTimeout = System.getenv("CONNECTION_REQUEST_TIMEOUT");
        return connectionRequestTimeout != null ? Integer.parseInt(connectionRequestTimeout) : parseWithDefault(configuration.getRequestTimeout(), DEFAULT_TIMEOUT);
    }

    public boolean isCacheAuthenticationEnabled() {
        return configuration.isAuthCache();
    }

    public byte[] getBasicAuthorization() {
        StringBuilder sb = new StringBuilder();
        return sb.append(getApplicationName()).append(":").append(getApplicationPassword()).toString().getBytes(StandardCharsets.UTF_8);
    }
}
