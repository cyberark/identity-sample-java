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

import com.sampleapp.entity.DBUser;
import com.sampleapp.entity.Response;
import com.sampleapp.entity.TokenStore;
import com.sampleapp.entity.User;
import com.sampleapp.entity.VerifyTotpReq;
import com.sampleapp.repos.TokenStoreRepository;
import com.sampleapp.repos.UserRepository;
import com.cyberark.client.Authentication;
import com.cyberark.client.UserManagement;
import com.cyberark.entities.AuthResponse;
import com.cyberark.entities.SignUpResponse;

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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserOpsService {

	Logger logger = LoggerFactory.getLogger(UserOpsService.class);

	@Autowired
	private UserRepository repo;

	@Autowired
	private TokenStoreRepository tokenStoreRepository;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private UserService userService;

	@LoadBalanced
	private final RestTemplate restTemplate;

	public UserOpsService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	private String getJson(User user) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(user);
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

	// This method updates user information in Idaptive Cloud directory.
	public ResponseEntity<JsonNode> updateUser(String token, String uuid, User user, Boolean enableMFAWidgetFlow)
			throws IOException {

		Response response;

		user.setUuid(uuid);
		String userJson = getJson(user);
		UserManagement userManagement = new UserManagement(settingsService.getTenantURL());
		SignUpResponse signUpResponse = userManagement.updateProfile(token, userJson).execute();

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.convertValue(signUpResponse, JsonNode.class);

			ObjectNode objNode = (ObjectNode) node;
			objNode.put("UserName", user.getName());

			if (enableMFAWidgetFlow.booleanValue()) {
				TokenStore tokenStore = (TokenStore) RequestContextHolder.currentRequestAttributes()
						.getAttribute("UserTokenStore", RequestAttributes.SCOPE_REQUEST);
				DBUser dbUser = repo.getById(tokenStore.getUserId());
				dbUser.setName(user.getName());
				dbUser.setDisplayName(user.getDisplayName());
				dbUser.setMail(user.getMail());
				dbUser.setMobileNumber((user.getMobileNumber()));
				repo.save(dbUser);
			}
			return new ResponseEntity(objNode, HttpStatus.OK);
		} catch (NullPointerException | RestClientException e) {
			logger.error("updateUser Exception occurred : ", e);
			return new ResponseEntity(new Response(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<JsonNode> getTotpQR(String token) {
		try {
			Authentication authentication = new Authentication(settingsService.getTenantURL());
			AuthResponse authResponse = authentication.getTotpQr(token).execute();
			return new ResponseEntity(authResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("getTotpQR Exception occurred : ", e);
			return new ResponseEntity(new Response(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Get user information using specified UUID
	public ResponseEntity<JsonNode> getUser(String uuid, String token) {
		try {
			HttpHeaders headers = prepareForRequest(token);
			HttpEntity<String> request = new HttpEntity<>("{\"ID\":\"" + uuid + "\"}", headers);
			String url = settingsService.getTenantURL() + "/CDirectoryService/GetUser";
			JsonNode response = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class).getBody();
			JsonNode result = response.get("Result");
			ObjectNode objNode = (ObjectNode) result;
			objNode.put(settingsService.getRoleName(), isRolePresent(uuid, token));
			return new ResponseEntity(response, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error("getUser Exception occurred : ", ex);
			return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
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

	public ResponseEntity<JsonNode> verifyTotp(String token, VerifyTotpReq req) {
		try {
			String verifyTotpJson = req.toJSONString();
			Authentication authentication = new Authentication(settingsService.getTenantURL());
			AuthResponse authResponse = authentication.validateTotp(token, verifyTotpJson).execute();
			return new ResponseEntity(authResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("verifyTotp Exception occurred : ", e);
			return new ResponseEntity(new Response(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<JsonNode> getChallengeID(String userCookie) {
		try {
			Response response = new Response();

			String oauthToken = userService.receiveOAuthTokenForClientCreds();
			String defaultAuthProfileID = getAppDetails(oauthToken);
			if (defaultAuthProfileID == null)
				return new ResponseEntity(response, HttpStatus.OK);

			String profileName = getProfileName(oauthToken, defaultAuthProfileID);
			String challengeID = challengeUser(userCookie, profileName);

			if (challengeID == null)
				return new ResponseEntity(response, HttpStatus.OK);

			response.Result = challengeID;
			return new ResponseEntity(response, HttpStatus.OK);

		} catch (Exception ex) {
			logger.error("getChallengeID Exception occurred : ", ex);
			return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String getAppDetails(String token) throws Exception {
		HttpHeaders headers = prepareForRequest(token);

		// Get OIDC App Details
		HttpEntity<String> appDetailsRequest = new HttpEntity<>(
				"{\"_RowKey\":\"" + settingsService.getOIDCClientID() + "\"}", headers);
		String appDetailsUrl = settingsService.getTenantURL() + "/saasManage/GetApplication";
		JsonNode appResponse = restTemplate.exchange(appDetailsUrl, HttpMethod.POST, appDetailsRequest, JsonNode.class)
				.getBody();

		List<String> defaultProfileOptions = new ArrayList<>(Arrays.asList("AlwaysAllowed", "-1", "--"));

		if (appResponse.get("success").asBoolean()) {
			String defaultAuthProfile = appResponse.get("Result").get("DefaultAuthProfile").asText();

			if (!defaultProfileOptions.contains(defaultAuthProfile)) {
				return defaultAuthProfile;
			}
		} else {
			throw new Exception(appResponse.get("Message").asText());
		}
		return null;
	}

	private String getProfileName(String token, String profileID) throws Exception {
		HttpHeaders headers = prepareForRequest(token);

		HttpEntity<String> getProfileRequest = new HttpEntity<>("{\"uuid\":\"" + profileID + "\"}", headers);
		String getProfileUrl = settingsService.getTenantURL() + "/AuthProfile/GetProfile";
		JsonNode response = restTemplate.exchange(getProfileUrl, HttpMethod.POST, getProfileRequest, JsonNode.class)
				.getBody();

		if (response.get("success").asBoolean()) {
			return response.get("Result").get("Name").asText();
		} else {
			throw new Exception(response.get("Result").asText());
		}
	}

	private String challengeUser(String userCookie, String profileName) throws Exception {
		HttpHeaders headers = prepareForRequest(userCookie);

		HttpEntity<String> challengeRequest = new HttpEntity<>("{\"profileName\":\"" + profileName + "\"}", headers);
		String challengeUrl = settingsService.getTenantURL() + "/Security/ChallengeUser";
		JsonNode response = restTemplate.exchange(challengeUrl, HttpMethod.POST, challengeRequest, JsonNode.class)
				.getBody();

		if (!response.get("success").asBoolean()) {
			if (response.get("Result") != null) {
				return response.get("Result").get("ChallengeId").asText();
			} else {
				throw new Exception(response.get("Message").asText());
			}
		}
		return null;
	}
}
