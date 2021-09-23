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

import com.sampleapp.Repos.TokenStoreRepository;
import com.sampleapp.Repos.UserRepository;
import com.sampleapp.entity.DBUser;
import com.sampleapp.entity.TokenStore;
import com.sampleapp.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.context.request.RequestContextHolder;

@Service
public class UserOpsService {

	Logger logger = LoggerFactory.getLogger(UserOpsService.class);

	@Autowired
	private UserRepository repo;

	@Autowired
	private TokenStoreRepository tokenStoreRepository;

	@Autowired
	private SettingsService settingsService;

	@LoadBalanced
	private final RestTemplate restTemplate;

	public UserOpsService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	private String getJson(User user) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String name = user.getName();
		user.setName(GetMFAUserName(name));
		try {
			String json =  mapper.writeValueAsString(user);
			user.setName(name);
			return json;
		} catch (Exception e) {
			logger.error("Exception occurred : ", e);
			throw e;
		}
	}

	private HttpHeaders setHeaders(String token) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("X-IDAP-NATIVE-CLIENT", "true");
		httpHeaders.set("content-type", "application/json");
		httpHeaders.set("cache-control", "no-cache");
		httpHeaders.set("Authorization", "Bearer " + token);
		return httpHeaders;
	}

	private HttpHeaders prepareForRequest(String token) {
		return setHeaders(token);
	}

	//This method updates user information in Idaptive Cloud directory.
	public ResponseEntity<JsonNode> updateUser(String token, String uuid, User user, Boolean enableMFAWidgetFlow) throws JsonProcessingException {
		user.setUuid(uuid);
		String userJson = getJson(user);
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>(userJson, headers);
		String updateUserUrl = settingsService.getTenantURL() + "/CDirectoryService/ChangeUser";
		try {
			ResponseEntity<JsonNode> result = restTemplate.exchange(updateUserUrl, HttpMethod.POST, request, JsonNode.class);
			JsonNode response = result.getBody();
			ObjectNode objNode = (ObjectNode) response;
			objNode.put("UserName",  GetMFAUserName(user.getName()));

			if(enableMFAWidgetFlow) {
				TokenStore tokenStore = (TokenStore) RequestContextHolder.currentRequestAttributes().getAttribute("UserTokenStore", 1);
				DBUser dbUser = repo.getOne(tokenStore.getUserId());
				dbUser.setName(user.getName());
				dbUser.setDisplayName(user.getDisplayName());
				dbUser.setMail(user.getMail());
				dbUser.setMobileNumber((user.getMobileNumber()));
				repo.save(dbUser);
			}
			return new ResponseEntity<JsonNode>(response, HttpStatus.OK);
		} catch (RestClientException e) {
			logger.error("Exception occurred : ", e);
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	//Get user information using specified UUID
	public ResponseEntity<JsonNode> getUser(String uuid, String token) {
		try{
			HttpHeaders headers = prepareForRequest(token);
			HttpEntity<String> request = new HttpEntity<>("{\"ID\":\"" + uuid + "\"}", headers);
			String url = settingsService.getTenantURL() + "/CDirectoryService/GetUser";
			JsonNode response = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class).getBody();
			JsonNode result = response.get("Result");
			String name = response.get("Result").get("Name").asText();
			String[] nameArr = name.split("@");
			ObjectNode objNode = (ObjectNode) result;
			objNode.remove("Name");
			objNode.put("Name", nameArr[0]);
			objNode.put(settingsService.getRoleName(), isRolePresent(uuid, token));
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception ex){
			logger.error("Exception occurred : ", ex);
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String GetMFAUserName(String name){
		return name + "@" + settingsService.getLoginSuffix();
	}

	public boolean isRolePresent(String uuid, String token) {
		String url = settingsService.getTenantURL() + "/UserMgmt/GetUsersRolesAndAdministrativeRights?id=" + uuid;
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>(headers);
		JsonNode result = restTemplate.postForObject(url, request, JsonNode.class);
		JsonNode arr = result.get("Result").get("Results");
		for (JsonNode jsonNode : arr) {
			if (jsonNode.get("Row").get("RoleName").asText().equals(settingsService.getRoleName())) {
				return true;
			}
		}
		return false;
	}
}
