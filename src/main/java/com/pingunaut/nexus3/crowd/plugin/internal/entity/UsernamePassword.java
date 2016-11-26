package com.pingunaut.nexus3.crowd.plugin.internal.entity;

public class UsernamePassword {

	private String username;
	private String password;

	public static UsernamePassword of(String username, char[] password) {
		UsernamePassword up = new UsernamePassword();
		up.setUsername(username);
		up.setPassword(new String(password));
		return up;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
