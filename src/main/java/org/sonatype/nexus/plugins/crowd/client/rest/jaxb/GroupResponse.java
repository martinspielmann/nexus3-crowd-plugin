/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.crowd.client.rest.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Issa Gorissen
 */
@XmlRootElement(name="group")
public class GroupResponse {
    /**
     * Capture any unknown elements that might be returned by newer crowd REST API versions, 
     * thus avoiding exceptions during JAXB unmarshalling
     */
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    @XmlAttribute
    public String name;

    public Link link;
    public String description;
    public String type;
    public boolean active;
    public Attributes attributes;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GroupResponse [name=").append(name).append(", link=")
        .append(link).append(", description=").append(description)
        .append(", type=").append(type).append(", active=")
        .append(active).append(", attributes=").append(attributes)
        .append("]");
        return builder.toString();
    }
}
