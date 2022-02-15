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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.cyberark.client.UserMgmt;
import com.cyberark.entities.SignUpResponse;
import com.cyberark.entities.TokenHolder;
import com.cyberark.requestBuilders.SignUpRequest;
import com.sampleapp.entity.AuthFlows;
import com.sampleapp.entity.AuthorizationFlow;
import com.sampleapp.entity.GrantType;
import com.sampleapp.entity.TokenMetadataRequest;
import com.sampleapp.entity.DBUser;
import com.sampleapp.entity.MfaUserMapping;
import com.sampleapp.entity.Response;
import com.sampleapp.exception.RoleNotFoundException;
import com.sampleapp.repos.MfaUserMappingRepository;
import com.sampleapp.repos.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sampleapp.entity.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

@Service
public class UserService {

	Logger logger = LoggerFactory.getLogger(UserService.class);

	@LoadBalanced
	private final RestTemplate restTemplate;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private UserRepository repo;

	@Autowired
	private MfaUserMappingRepository mfaUserMappingRepository;

	@Autowired
	public AuthFlows authFlows;

	public UserService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	private String getJson(User user) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String name = user.getName();
		user.setName(GetMFAUserName(name));
		try {
			String json = mapper.writeValueAsString(user);
			user.setName(name);
			return json;
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred : ", e);
			throw e;
		}
	}

	public String receiveOAuthTokenForClientCreds() throws Exception {
		TokenMetadataRequest metadataRequest = new TokenMetadataRequest();
		metadataRequest.grantType = GrantType.client_credentials;

		TokenHolder tokenHolder = null;
		try {
			tokenHolder = this.authFlows.getEnumMap().get(AuthorizationFlow.OAUTH)
					.getTokenSetWithClientCreds(metadataRequest);
		} catch (IOException e) {
			logger.error("Error occurred while fetching access_token : ", e);
			throw new Exception(e.getMessage(), e);
		}

		return tokenHolder.getAccessToken();
	}

	private HttpHeaders setHeaders(String token) {
		HttpServletRequest currentRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		HttpHeaders httpHeaders = new HttpHeaders();

		httpHeaders.set("X-IDAP-NATIVE-CLIENT", "true");
		httpHeaders.set("content-type", "application/json");
		httpHeaders.set("cache-control", "no-cache");
		httpHeaders.set("Authorization", "Bearer " + token);
		if (currentRequest.getHeader("CLIENT_IP") != null) {
			httpHeaders.set("X_FORWARDED_FOR", currentRequest.getHeader("CLIENT_IP"));
		}
		return httpHeaders;
	}

	private HttpHeaders prepareForRequestOauth() throws Exception {
		String token = receiveOAuthTokenForClientCreds();
		return setHeaders(token);
	}

	public ResponseEntity<JsonNode> createUser(User user, boolean enableMFAWidgetFlow) throws Exception {
		try {
			UserMgmt userMgmt = new UserMgmt(settingsService.getTenantURL());
			SignUpRequest signUpRequest = userMgmt.signUpWithCaptcha(user.ReCaptchaToken)
					.setUserName(GetMFAUserName(user.getName()))
					.setPassword(String.valueOf(user.getPassword()));

			Map<String, Object> attributes = new HashMap<>();
			attributes.put("Mail", user.getMail());
			attributes.put("MobileNumber", user.getMobileNumber());
			SignUpResponse signUpResponse = signUpRequest.setAdditionalAttributes(attributes)
					.execute();

			if (!signUpResponse.isSuccess() && signUpResponse.getResult().isNull()) {
				return new ResponseEntity(signUpResponse, HttpStatus.OK);
			} else {
				HttpHeaders headers = prepareForRequestOauth();
				String updateRoleUrl = settingsService.getTenantURL() + "/Roles/UpdateRole";

				String roleUuid = getRoleUuid(settingsService.getRoleName());
				HttpEntity<String> updateRoleRequest = new HttpEntity<>(
						"{\"Users\":{\"Add\":[\"" + signUpResponse.getResult().get("UserId").asText()
								+ "\"]},\"Name\":\"" + roleUuid + "\",\"Description\":\"\"}",
						headers);
				restTemplate.exchange(updateRoleUrl, HttpMethod.POST, updateRoleRequest, JsonNode.class);

				if (enableMFAWidgetFlow) {
					saveToCustomDb(user, signUpResponse.getResult().get("UserId").asText());
				}
			}

			Arrays.fill(user.password, ' ');

			return new ResponseEntity(signUpResponse, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred : ", e);
			return new ResponseEntity(new Response(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void saveToCustomDb(User user, String mfaUserId) {
		DBUser outUser = repo.save(user.getUser());
		mfaUserMappingRepository.save(new MfaUserMapping(outUser.getId(), mfaUserId));
	}

	public String getRoleUuid(String roleName) throws RoleNotFoundException, Exception {
		String getRoles = settingsService.getTenantURL() + "/Redrock/query";
		HttpHeaders headers = prepareForRequestOauth();
		HttpEntity<String> getRolesRequest = new HttpEntity<>(
				"{ \"Script\": \"Select ID as ID from Role WHERE Name = \'" + roleName
						+ "\' ORDER BY Name COLLATE NOCASE \"}",
				headers);
		ResponseEntity<JsonNode> getRoleInfo = restTemplate.exchange(getRoles, HttpMethod.POST, getRolesRequest,
				JsonNode.class);
		JsonNode node = getRoleInfo.getBody().get("Result").get("Results");
		String roleUuid = null;
		if (node.size() == 1) {
			for (JsonNode objNode : node) {
				if (objNode.has("Row")) {
					return objNode.get("Row").get("ID").asText();
				}
			}
		} else {
			throw new RoleNotFoundException(roleName);
		}
		return roleUuid;
	}

	public DBUser Get(String name, char[] password) {
		DBUser exampleUser = new DBUser();
		exampleUser.setName(name);
		exampleUser.setPassword(password);
		try {
			DBUser dbUser = repo.findOne(Example.of(exampleUser)).get();
			Arrays.fill(exampleUser.Password, ' ');
			return dbUser;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return null;
		}
	}

	public DBUser Get(Integer id) {
		return repo.findById(id).get();
	}

	public String GetMFAUserName(String name) {
		return name + "@" + settingsService.getLoginSuffix();
	}
}
