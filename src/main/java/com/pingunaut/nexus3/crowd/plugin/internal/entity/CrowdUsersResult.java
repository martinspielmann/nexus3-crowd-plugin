package com.pingunaut.nexus3.crowd.plugin.internal.entity;

import java.util.ArrayList;
import java.util.List;

public class CrowdUsersResult {

	private List<CrowdUserResult> users = new ArrayList<>();

	public List<CrowdUserResult> getUsers() {
		return users;
	}

	public void setUsers(List<CrowdUserResult> users) {
		this.users = users;
	}

}
