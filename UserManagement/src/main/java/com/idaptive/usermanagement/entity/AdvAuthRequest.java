package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class AdvAuthRequest {

	@JsonProperty("TenantId")
	private String tenantId;

	@JsonProperty("SessionId")
	private String sessionId;

	@JsonProperty("MechanismId")
	private String mechanismId;

	@JsonProperty("Action")
	private String action;

	@JsonProperty("Answer")
	private String answer;

	@JsonProperty("User")
	private String username;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getMechanismId() {
		return mechanismId;
	}

	public void setMechanismId(String mechanismId) {
		this.mechanismId = mechanismId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "AdvAuthRequest [tenantId=" + tenantId + ", sessionId=" + sessionId + ", mechanismId=" + mechanismId
				+ ", action=" + action + ", answer=" + answer + ", username=" + username + "]";
	}

}
