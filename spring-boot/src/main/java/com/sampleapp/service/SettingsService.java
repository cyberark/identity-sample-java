/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SettingsService {
	private static final String SETTINGS_FILE_PATH = Paths.get("settings.json").toAbsolutePath().toString();

	Logger logger = LoggerFactory.getLogger(SettingsService.class);
	private JsonNode settings;

	public SettingsService() throws Exception {
		LoadUserSettings();
	}

	public String getTenantURL() { return settings.get("tenantURL").textValue(); }

	public String getLoginSuffix() { return settings.get("loginSuffix").textValue(); }

	public String getRoleName() { return settings.get("roleName").textValue(); }

	public String getMFAWidgetID() { return settings.get("mfaWidgetId").textValue(); }

	public String getLoginWidgetID() { return settings.get("loginWidgetId").textValue(); }

	public String getOauthApplicationID() { return settings.get("oauthAppId").textValue(); }

	public String getOauthServiceUserName() { return settings.get("oauthServiceUserName").textValue(); }

	public char[] getOauthServiceUserPass() { return settings.get("oauthServiceUserPassword").textValue().toCharArray(); }

	public String getOauthScopesSupported() { return settings.get("oauthScopesSupported").textValue(); }

	public String getOIDCApplicationID() { return settings.get("oidcAppId").textValue(); }

	public String getOIDCClientID() { return settings.get("oidcClientId").textValue(); }

	public String getOIDCScopesSupported() { return settings.get("oidcScopesSupported").textValue(); }


	/** Updates Settings to settings.json file.
	 * @param body as Settings JSON
	 * @throws IOException
	 */
	public void updateSettings(JsonNode body) throws IOException {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(SETTINGS_FILE_PATH));
			writer.write((new ObjectMapper()).writeValueAsString(body));
			writer.close();
			settings = body.deepCopy();
		} catch (Exception e) {
			logger.error("Settings not updated.", e);
			throw e;
		}
	}

	/** Get Settings from runtime object loaded with settings.json content
	 * @return JsonNode as settings
	 */
	public JsonNode getSettings() {
		return settings;
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
}
