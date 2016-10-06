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

/**
 * Interface that manages Crowd Plugin Configuration data.
 *
 */
public interface CrowdPluginConfiguration {

    /**
     * Get the application name registered with Crowd.
     * 
     * @return String
     */
    public String getApplicationName();

    /**
     * Get the application password registered with Crowd.
     * 
     * @return String
     */
    public String getApplicationPassword();

    /**
     * Get time to live (seconds) for entries stored into cache (user's details,
     * users' nested groups, all crowd groups).
     * 
     * @return int
     */
    public int getCacheTTL();

    /**
     * Get the Crowd Server URL.
     * 
     * @return String
     */
    public String getCrowdServerUrl();

    /**
     * Get the maximum number of HTTP connections in the connection pool for
     * communication with the Crowd server.
     * 
     * @return int
     */
    public int getHttpMaxConnections();

    /**
     * Get the name of the proxy server used to transport SOAP traffic to the
     * Crowd server.
     * 
     * @return String
     */
    public String getHttpProxyHost();

    /**
     * Get the password used to authenticate with the proxy server (if the proxy
     * server requires authentication).
     * 
     * @return String
     */
    public String getHttpProxyPassword();

    /**
     * Get the connection port of the proxy server (must be specified if a proxy
     * host is specified).
     * 
     * @return int
     */
    public int getHttpProxyPort();

    /**
     * Get the username used to authenticate with the proxy server (if the proxy
     * server requires authentication).
     * 
     * @return String
     */
    public String getHttpProxyUsername();

    /**
     * Get the HTTP connection timeout (milliseconds) used for communication
     * with the Crowd server. A value of zero indicates that there is no
     * connection timeout.
     * 
     * @return int
     */
    public int getHttpTimeout();
}
