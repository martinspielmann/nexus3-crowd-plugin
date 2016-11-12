package org.sonatype.nexus.crowd.plugin.internal.entity;

import com.google.gson.annotations.SerializedName;

public class CrowdUserResult {

	private String name;

	@SerializedName("first-name")
	private String firstName;

	@SerializedName("last-name")
	private String lastName;

	private boolean active;

	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
