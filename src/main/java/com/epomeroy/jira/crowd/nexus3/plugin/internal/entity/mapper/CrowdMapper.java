package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.mapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import com.epomeroy.jira.crowd.nexus3.plugin.internal.CrowdUserManager;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.AuthenticationResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdGroupResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdGroupsResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdUserResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdUsersResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.Password;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CrowdMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdMapper.class);

    private static final Gson GSON = new Gson();

    private CrowdMapper() {
    }

    public static User toUser(CrowdUserResult c) {
        User u = new User();
        u.setEmailAddress(c.getEmail());
        u.setFirstName(c.getFirstName());
        u.setLastName(c.getLastName());
        u.setReadOnly(true);
        u.setStatus(c.isActive() ? UserStatus.active : UserStatus.disabled);
        u.setUserId(c.getName());
        u.setSource(CrowdUserManager.SOURCE);
        return u;
    }

    public static Role toRole(CrowdGroupResult crowdGroup) {
        return new Role(crowdGroup.getName(), crowdGroup.getName(), crowdGroup.getDescription(),
                CrowdUserManager.SOURCE, true, null, null);
    }

    public static String toPasswordJsonString(char[] password) {
        return GSON.toJson(Password.of(password));
    }

    public static String toAuthToken(HttpResponse r) {
        if (r.getStatusLine().getStatusCode() == 200) {
            try {
                return GSON.fromJson(EntityUtils.toString(r.getEntity()), AuthenticationResult.class).getName();
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return null;
    }

    public static Set<String> toRoleStrings(HttpResponse r) {
        if (responseOK(r)) {
            try {
                CrowdGroupsResult result = GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupsResult.class);
                return result.getGroups().stream().map(CrowdGroupResult::getName).collect(Collectors.toSet());
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return Collections.emptySet();
    }

    private static boolean responseOK(HttpResponse r) {
        return r.getStatusLine().getStatusCode() == 200;
    }

    public static User toUser(HttpResponse r) {
        if (responseOK(r)) {
            try {
                return toUser(GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdUserResult.class));
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return null;
    }

    public static Role toRole(HttpResponse r) {
        if (responseOK(r)) {
            try {
                return toRole(GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupResult.class));
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return null;
    }

    public static Set<User> toUsers(HttpResponse r) {
        if (responseOK(r)) {
            try {
                return GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdUsersResult.class).getUsers().stream()
                        .map(CrowdMapper::toUser).collect(Collectors.toSet());
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return Collections.emptySet();
    }

    public static Set<Role> toRoles(HttpResponse r) {
        if (responseOK(r)) {
            try {
                return GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupsResult.class).getGroups().stream()
                        .map(CrowdMapper::toRole).collect(Collectors.toSet());
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return Collections.emptySet();
    }

    private static void logMappingException(Exception e) {
        LOGGER.error("Error while mapping result", e);
    }

    private static void logResponseException(HttpResponse r) {
        String content = "";
        try {
            content = IOUtils.toString(r.getEntity().getContent());
        } catch (IOException e) {
            // no content available. just log status code
        }
        LOGGER.error(String.format("Error with request %s - STATUS %d", content, r.getStatusLine().getStatusCode()));
    }
}
