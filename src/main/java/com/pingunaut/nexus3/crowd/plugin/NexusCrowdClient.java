package com.pingunaut.nexus3.crowd.plugin;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserSearchCriteria;

import java.util.Set;

/**
 * The Interface NexusCrowdClient provides all methods to be used to retrieve
 * user and role information from crowd.
 */
public interface NexusCrowdClient {

	/**
	 * Authenticate.
	 *
	 * @param token
	 *            the token
	 * @return true, if successful
	 */
	boolean authenticate(UsernamePasswordToken token);

	/**
	 * Find user by username.
	 *
	 * @param username
	 *            the username
	 * @return the user
	 */
	User findUserByUsername(String username);

	/**
	 * Find all usernames.
	 *
	 * @return the sets the
	 */
	Set<String> findAllUsernames();

	/**
	 * Find users.
	 *
	 * @return the sets the
	 */
	Set<User> findUsers();

	/**
	 * Find user by criteria.
	 *
	 * @param criteria
	 *            the criteria
	 * @return the sets the
	 */
	Set<User> findUserByCriteria(UserSearchCriteria criteria);

	/**
	 * Find roles.
	 *
	 * @return the sets the
	 */
	Set<Role> findRoles();

	/**
	 * Find role by role id.
	 *
	 * @param roleId
	 *            the role id
	 * @return the role
	 */
	Role findRoleByRoleId(String roleId);

	/**
	 * Find roles by user.
	 *
	 * @param username
	 *            the username
	 * @return the sets the
	 */
	Set<String> findRolesByUser(String username);

}
