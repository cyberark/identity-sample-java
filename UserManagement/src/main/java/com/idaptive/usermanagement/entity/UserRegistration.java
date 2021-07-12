package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegistration{

	@JsonProperty("user")
	private User user;

	@JsonProperty("isMfa")
	private boolean isMfa;

	public UserRegistration(){

	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean getIsMfa() {
		return isMfa;
	}

	public void setIsMfa(boolean isMfa) {
		this.isMfa = isMfa;
	}

}