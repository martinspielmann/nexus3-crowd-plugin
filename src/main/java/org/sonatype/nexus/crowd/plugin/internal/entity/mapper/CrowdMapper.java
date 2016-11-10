package org.sonatype.nexus.crowd.plugin.internal.entity.mapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.crowd.plugin.internal.CrowdUserManager;
import org.sonatype.nexus.crowd.plugin.internal.entity.CrowdGroupResult;
import org.sonatype.nexus.crowd.plugin.internal.entity.CrowdGroupsResult;
import org.sonatype.nexus.crowd.plugin.internal.entity.CrowdTokenResult;
import org.sonatype.nexus.crowd.plugin.internal.entity.CrowdUserResult;
import org.sonatype.nexus.crowd.plugin.internal.entity.CrowdUsersResult;
import org.sonatype.nexus.crowd.plugin.internal.entity.UsernamePassword;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserStatus;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CrowdMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrowdMapper.class);

	public static Gson gson = new Gson();

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

	public static String toUsernamePasswordJsonString(String username, char[] password) {
		return gson.toJson(UsernamePassword.of(username, password));
	}

	public static String toAuthToken(HttpResponse r) {
		if (r.getStatusLine().getStatusCode() == 201) {
			try {
				return gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdTokenResult.class).getToken();
			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return null;
	}

	public static Set<String> toRoleStrings(HttpResponse r) {
		if (responseOK(r)) {
			try {
				CrowdGroupsResult result = gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupsResult.class);
				return result.getGroups().stream().map(CrowdGroupResult::getName).collect(Collectors.toSet());

			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return Collections.emptySet();
	}

	private static boolean responseOK(HttpResponse r) {
		return r.getStatusLine().getStatusCode() == 200;
	}

	public static User toUser(HttpResponse r) {
		if (responseOK(r)) {
			try {
				return toUser(gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdUserResult.class));
			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return null;
	}

	public static Role toRole(HttpResponse r) {
		if (responseOK(r)) {
			try {
				return CrowdMapper.toRole(gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupResult.class));
			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return null;
	}

	public static Set<User> toUsers(HttpResponse r) {
		if (responseOK(r)) {
			try {
				return gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdUsersResult.class).getUsers().stream()
						.map(CrowdMapper::toUser).collect(Collectors.toSet());
			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return Collections.emptySet();
	}

	public static Set<Role> toRoles(HttpResponse r) {
		if (responseOK(r)) {
			try {
				return gson.fromJson(EntityUtils.toString(r.getEntity()), CrowdGroupsResult.class).getGroups().stream()
						.map(CrowdMapper::toRole).collect(Collectors.toSet());
			} catch (JsonSyntaxException | ParseException | IOException e) {
				LOGGER.error("Error while mapping result", e);
			}
		} else {
			LOGGER.error(String.format("Error with request %s - %d", r.getEntity(), r.getStatusLine().getStatusCode()));
		}
		return Collections.emptySet();
	}
}
