package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.mapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import com.epomeroy.jira.crowd.nexus3.plugin.internal.CrowdUserManager;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.AuthenticationResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdGroupResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdGroupsResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdUserResult;
import com.epomeroy.jira.crowd.nexus3.plugin.internal.entity.CrowdUsersResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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

    public static String toAuthenticationJsonString(UsernamePasswordToken token) {
        JsonObject auth = new JsonObject();
        auth.addProperty("username", token.getUsername());
        auth.addProperty("password", new String(token.getPassword()));

        return auth.toString();
    }

    public static String toAuthToken(HttpResponse r) {
        if (r.getStatusLine().getStatusCode() == 200) {
            try {
                return GSON.fromJson(EntityUtils.toString(r.getEntity()), AuthenticationResult.class).getSession().getValue();
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
                CrowdGroupsResult result = GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdUserResult.class).getGroups();
                return result.getItems().stream().map(CrowdGroupResult::getName).collect(Collectors.toSet());
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

    public static Set<User> toUsers(HttpResponse r) {
        if (responseOK(r)) {
            try {
                return GSON.fromJson(EntityUtils.toString(r.getEntity()), CrowdUsersResult.class).getValues().stream()
                        .map(CrowdMapper::toUser).collect(Collectors.toSet());
            } catch (JsonSyntaxException | ParseException | IOException e) {
                logMappingException(e);
            }
        } else {
            logResponseException(r);
        }
        return Collections.emptySet();
    }

    public static Set<User> toUserSearch(HttpResponse r) {
        if (responseOK(r)) {
            try {
                Type listType = new TypeToken<List<CrowdUserResult>>() {
                }.getType();
                return ((List<CrowdUserResult>) GSON.fromJson(EntityUtils.toString(r.getEntity()), listType))
                        .stream()
                        .map(CrowdMapper::toUser)
                        .collect(Collectors.toSet());
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
