package com.idaptive.usermanagement.service;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ConfigService {
	Logger logger = LoggerFactory.getLogger(ConfigService.class);

	public ResponseEntity<JsonNode> updateConfig(JsonNode body) {
		try {
			PropertiesConfiguration userConfig = new PropertiesConfiguration("application.yml");
			ObjectNode objNode = (ObjectNode) body;
			objNode.remove("Username");
			Iterator<String> it = body.fieldNames();
			while (it.hasNext()) {
				String fieldName = it.next();
				userConfig.setProperty(fieldName, body.get(fieldName).asText());
			}
			userConfig.save();
			JsonNode response = null;
			String message = "{\"success\":true,\"Result\":{\"message\":\"Updated  configuration file Successfully.\"}}";
			ObjectMapper mapper = new ObjectMapper();
			response = mapper.readTree(message);
			return new ResponseEntity<JsonNode>(response, HttpStatus.OK);
		} catch (IOException | ConfigurationException e) {
			JsonNode response = null;
			String message = "{\"success\":true,\"Result\":{\"message\":\"Configuration file not updated.\"}}";
			ObjectMapper mapper = new ObjectMapper();
			try {
				response = mapper.readTree(message);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return new ResponseEntity<JsonNode>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<JsonNode> getConfig() {

		try {
			PropertiesConfiguration userConfig = new PropertiesConfiguration("application.yml");
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("tenant", userConfig.getProperty("tenant").toString());
			objectNode.put("customerId", userConfig.getProperty("customerId").toString());
			objectNode.put("oauthAppId", userConfig.getProperty("oauthAppId").toString());
			objectNode.put("oauthUser", userConfig.getProperty("oauthUser").toString());
			objectNode.put("oauthPassword", userConfig.getProperty("oauthPassword").toString());
			objectNode.put("mfaRole", userConfig.getProperty("mfaRole").toString());
			objectNode.put("accentColor", userConfig.getProperty("accentColor").toString());
			objectNode.put("ribbonColor", userConfig.getProperty("ribbonColor").toString());
			objectNode.put("appImage", userConfig.getProperty("appImage").toString());
			JsonNode resp = objectNode;
			return new ResponseEntity<JsonNode>(resp, HttpStatus.OK);
		} catch (ConfigurationException e) {
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	public ResponseEntity<JsonNode> getClientConfig() {
		try {
			PropertiesConfiguration userConfig = new PropertiesConfiguration("application.yml");
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("accentColor", userConfig.getProperty("accentColor").toString());
			objectNode.put("ribbonColor", userConfig.getProperty("ribbonColor").toString());
			objectNode.put("appImage", userConfig.getProperty("appImage").toString());
			JsonNode resp = objectNode;
			return new ResponseEntity<JsonNode>(resp, HttpStatus.OK);
		} catch (ConfigurationException e) {
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
