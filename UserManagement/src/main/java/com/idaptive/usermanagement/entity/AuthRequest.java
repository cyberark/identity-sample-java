package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequest {

	@JsonProperty("User")
	private String username;

	@JsonProperty("Version")
	private String version;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
