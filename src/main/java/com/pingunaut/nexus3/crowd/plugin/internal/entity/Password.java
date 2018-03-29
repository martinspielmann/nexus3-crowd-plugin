package com.pingunaut.nexus3.crowd.plugin.internal.entity;

public class Password {

	private String value;

	public static Password of(char[] password) {
		Password p = new Password();
		p.setValue(new String(password));
		return p;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
