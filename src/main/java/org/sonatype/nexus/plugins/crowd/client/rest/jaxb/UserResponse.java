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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Issa Gorissen
 */
@XmlRootElement(name="user")
public class UserResponse {
    /**
     * Capture any unknown elements that might be returned by newer crowd REST API versions, 
     * thus avoiding exceptions during JAXB unmarshalling
     */
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    @XmlAttribute
    public String name;

    public Link link;

    @XmlElement(name="first-name")
    public String firstName;

    @XmlElement(name="last-name")
    public String lastName;

    @XmlElement(name="display-name")
    public String displayName;

    public String email;
    public Password password;
    public String key;
    public boolean active;
    public Attributes attributes;


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserResponse [name=").append(name).append(", link=").append(link)
        .append(", firstName=").append(firstName).append(", lastName=").append(lastName)
        .append(", displayName=").append(displayName).append(", email=").append(email)
        .append(", password=").append(password).append(", key=").append(key)
        .append(", active=").append(active).append(", attributes=").append(attributes)
        .append("]");
        return builder.toString();
    }
}

class Password {
    public Link link;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Password [link=").append(link).append("]");
        return builder.toString();
    }
}