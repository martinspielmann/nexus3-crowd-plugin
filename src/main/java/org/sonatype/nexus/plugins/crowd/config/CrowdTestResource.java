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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sonatype.nexus.plugins.crowd.client.rest.RestClient;
import org.sonatype.nexus.plugins.crowd.client.rest.RestException;
import org.sonatype.nexus.rest.Resource;


/**
 * Intent of this class is to enable an admin to easily test if the Crowd
 * connection is working <b>without</b> enabling the Realm.
 * 
 * Can be tested through http://localhost:8081/service/siesta/crowd/test on a default Nexus setup
 * 
 * @author Justin Edelson
 * @author Issa Gorissen
 */
@Singleton
@Named
@Path(CrowdTestResource.RESOURCE_URI)
public class CrowdTestResource implements Resource {
    public static final String RESOURCE_URI = "/crowd/test";

    @Inject
    private RestClient restClient;

    @GET
    @Produces({MediaType.APPLICATION_XML})
    public String get() throws RestException {
        restClient.getCookieConfig();
        return "<status>OK</status>";
    }
}
