/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sampleapp.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

import com.sampleapp.entity.Response;
import com.sampleapp.entity.UISettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SettingsService {
	private static final String SETTINGS_FILE_PATH = Paths.get("settings.json").toAbsolutePath().toString();
	private static final String SYS_ADMIN_ROLE = "System Administrator";

	@LoadBalanced
	private final RestTemplate restTemplate;

	Logger logger = LoggerFactory.getLogger(SettingsService.class);
	private JsonNode settings;

	public SettingsService(RestTemplateBuilder builder) throws Exception {
		this.restTemplate = builder.build();
		LoadUserSettings();
	}

	public String getTenantURL() { return settings.get("tenantUrl").textValue(); }

	public String getRoleName() { return settings.get("roleName").textValue(); }

	public String getOauthApplicationID() { return settings.get("oauthAppId").textValue(); }

	public String getOauthServiceUserName() { return settings.get("oauthServiceUserName").textValue(); }

	public char[] getOauthServiceUserPass() { return settings.get("oauthServiceUserPassword").textValue().toCharArray(); }

	public String getOauthScopesSupported() { return settings.get("oauthScopesSupported").textValue(); }

	public String getOIDCApplicationID() { return settings.get("oidcAppId").textValue(); }

	public String getOIDCClientID() { return settings.get("oidcClientId").textValue(); }

	public char[] getOIDCClientPass() { return settings.get("oidcClientPassword").textValue().toCharArray(); }

	public String getOIDCScopesSupported() { return settings.get("oidcScopesSupported").textValue(); }

	public long getSessionInactiveTimeInSec() { return settings.get("sessionTimeout").asLong(); }

	public long getMobileInactiveTimeInSec() { return settings.get("mobileTimeout").asLong(); }

	public boolean isCaptchaEnabledInSettings() { return settings.get("isCaptchaEnabledInSettings").asBoolean(); }

	/** Updates Settings to settings.json file.
	 * @param body as Settings JSON
	 * @throws IOException
	 */
	public ResponseEntity<JsonNode> updateSettings(JsonNode body, String uuid, String token, Boolean isSettingsLocked) throws IOException {
		Response response = new Response();
		try {
			if (isSettingsLocked) {
				Boolean isSysAdmin = this.isSysAdmin(uuid, token);
				if (!isSysAdmin) {
					throw new Exception("User not authorized to save settings.");
				} 
			}
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(SETTINGS_FILE_PATH));
			writer.write((new ObjectMapper()).writeValueAsString(body));
			writer.close();
			settings = body.deepCopy();
			response.Result = "Settings updated successfully";
			return new ResponseEntity(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred : ", e);
			response.Success = false;
			response.ErrorMessage = e.getMessage();
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/** Get Settings from runtime object loaded with settings.json content
	 * @return JsonNode as settings
	 */
	public UISettings getUISettings() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.treeToValue(settings, UISettings.class);
	}

	public ResponseEntity<JsonNode> getSettings(String uuid, String token) {
		Response response = new Response();
		try {
			Boolean isSysAdmin = this.isSysAdmin(uuid, token);
			if (!isSysAdmin) {
				throw new Exception("User not authorized to access settings.");
			}
			response.Result = settings;
			return new ResponseEntity(response, HttpStatus.OK);

		} catch (Exception ex) {
			logger.error("Exception occurred : ", ex);
			response.Success = false;
			response.ErrorMessage = ex.getMessage();
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**	Loads User Settings from settings.json file.
	 * @throws IOException
	 */
	public void LoadUserSettings() throws IOException {
		File file = new File(SETTINGS_FILE_PATH);
		if (!file.isFile() && !file.createNewFile())
		{
			throw new IOException("Error creating new file: " + file.getAbsolutePath());
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		settings = (new ObjectMapper()).readTree(reader);
	}

	public boolean isSysAdmin(String uuid, String token) {
		try {
			String url = this.getTenantURL() + "/UserMgmt/GetUsersRolesAndAdministrativeRights?id=" + uuid;
			HttpHeaders headers = UserOpsService.setHeaders(token);
			HttpEntity<String> request = new HttpEntity<>(headers);
			JsonNode result = restTemplate.postForObject(url, request, JsonNode.class);
			JsonNode arr = result.get("Result").get("Results");
			for (JsonNode jsonNode : arr) {
				if (jsonNode.get("Row").get("RoleName").asText().equals(SYS_ADMIN_ROLE)) {
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Exception encountered at isSysAdmin()", ex);
			return false;
		}
	}
}
