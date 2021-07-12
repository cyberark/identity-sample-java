package com.idaptive.usermanagement.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.idaptive.usermanagement.service.ConfigService;

@RestController
public class ConfigController {

	@Autowired
	private ConfigService configService;

	@PutMapping("config/updateconfig")
	public ResponseEntity<JsonNode> updateUserConfig(@RequestBody JsonNode body, HttpServletRequest request) {
		return configService.updateConfig(body);
	}

	@GetMapping("config/getconfig")
	public ResponseEntity<JsonNode> getUserConfig() {
		return configService.getConfig();
	}
	
	@GetMapping("config/getclientconfig")
	public ResponseEntity<JsonNode> getClientConfig() {
		return configService.getClientConfig();
	}
}
