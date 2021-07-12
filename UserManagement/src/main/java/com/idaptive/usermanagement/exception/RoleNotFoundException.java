package com.idaptive.usermanagement.exception;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoleNotFoundException extends Exception {

	private final String roleName;

	public RoleNotFoundException(String roleName) {
		this.roleName = roleName;
	}

	public JsonNode exceptionBody() {
		JsonNode body = null;
		String message = "{\"success\": false,\"Result\": {\"Summary\": \"Failure\"},\"Message\": \"Please contact your system administrator.Configured MFA Role not present.\"}";
		ObjectMapper mapper = new ObjectMapper();
		try {
			body = mapper.readTree(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}
}
