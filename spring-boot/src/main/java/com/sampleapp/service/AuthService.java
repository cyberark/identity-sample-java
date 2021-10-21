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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.sampleapp.Repos.MfaUserMappingRepository;
import com.sampleapp.Repos.TokenStoreRepository;
import com.sampleapp.entity.AdvanceLoginRequest;
import com.sampleapp.entity.AuthRequest;
import com.sampleapp.entity.DBUser;
import com.sampleapp.entity.TokenStore;
import com.sampleapp.entity.GrantType;
import com.sampleapp.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.web.context.request.RequestContextHolder;


@Service
public class AuthService {

	private final Logger logger = LoggerFactory.getLogger(AuthService.class);

	@Autowired
	private TokenStoreRepository tokenStoreRepository;
	
	@Autowired
	private MfaUserMappingRepository mfaUserMappingRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private SettingsService settingsService;

	@LoadBalanced
	private final RestTemplate restTemplate;

	@Value("${demoAppBaseURL}")
	public String demoAppBaseURL;

	public AuthService(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
	}

	private HttpHeaders setHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("X-IDAP-NATIVE-CLIENT", "true");
		httpHeaders.set("content-type", "application/json");
		httpHeaders.set("cache-control", "no-cache");
		return httpHeaders;
	}

	public ResponseEntity<JsonNode> startAuthenticationWithObject(AuthRequest authRequest, HttpServletResponse response, Boolean enableMFAWidgetFlow)
			throws JsonProcessingException {
		String tenant = settingsService.getTenantURL() + "/Security/StartAuthentication";
		HttpHeaders httpHeaders = setHeaders();
		ObjectMapper mapper = new ObjectMapper();
		String name = authRequest.getUsername();
		authRequest.setUsername(name + "@" + settingsService.getLoginSuffix());
		String beginAuth = mapper.writeValueAsString(authRequest);
		HttpEntity<String> request = new HttpEntity<>(beginAuth, httpHeaders);
		boolean success = false;
		ResponseEntity<JsonNode> responseObj = null;
		do {
			ResponseEntity<JsonNode> idaptiveResponse = restTemplate.exchange(tenant, HttpMethod.POST, request,
					JsonNode.class);
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set("content-type", "application/json");
			JsonNode result = idaptiveResponse.getBody();
			if (result.get("success").asBoolean()) {
				String auth = result.get("Result").has("Auth") ? result.get("Result").get("Auth").asText() : "";
				if (auth.length() == 0) {
					success = true;
					responseObj = new ResponseEntity<>(idaptiveResponse.getBody(), responseHeaders, HttpStatus.OK);
				} else {
					logout(auth, response, enableMFAWidgetFlow);
				}
			} else {
				success = true;
				responseObj = new ResponseEntity<>(idaptiveResponse.getBody(), responseHeaders, HttpStatus.BAD_REQUEST);
			}
		} while (!success);
		return responseObj;
	}

	public ResponseEntity<JsonNode> advanceAuthenticationByObject(JsonNode authRequest, HttpServletResponse response) throws UnsupportedEncodingException {
		try {
			String url = settingsService.getTenantURL() + "/Security/AdvanceAuthentication";
			HttpHeaders httpHeaders = setHeaders();
			HttpEntity<JsonNode> request = new HttpEntity<>(authRequest, httpHeaders);
			ResponseEntity<JsonNode> advAuthResp = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
			JsonNode advAuthBody = advAuthResp.getBody();
			HttpHeaders advAuthHeader = advAuthResp.getHeaders();
			if (advAuthResp.getBody().get("Result").has("UserId")) {
				String token = null;
				for (String value : advAuthHeader.get("Set-Cookie")) {
					if (value.split(";")[0].split("=")[0].equals(".ASPXAUTH")) {
						token = value.split(";")[0].split("=")[1];

						Cookie authCookie = new Cookie("AUTH", URLEncoder.encode(token, "UTF-8"));
						authCookie.setSecure(true);
						authCookie.setPath("/");
						response.addCookie(authCookie);
					}
				}
				return new ResponseEntity<JsonNode>(advAuthBody, advAuthHeader, HttpStatus.OK);
			} else {
				return advAuthResp;
			}
		} catch (Exception ex){
			logger.error("Exception occurred : ", ex);
			return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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

	public ResponseEntity<JsonNode> logout(String authToken, HttpServletResponse respose, Boolean enableMFAWidgetFlow) {
		try {
			String tenant = settingsService.getTenantURL() + "/Security/Logout";
			HttpHeaders headers = setHeaders();
			headers.set("Authorization", "Bearer " + authToken);
			HttpEntity<String> request = new HttpEntity<>(headers);
			Cookie cookie = new Cookie(".ASPXAUTH", null);
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			cookie.setMaxAge(0);
			respose.addCookie(cookie);
			Cookie authCookie = new Cookie("AUTH", null);
			authCookie.setPath("/");
			authCookie.setMaxAge(0);
			respose.addCookie(authCookie);
			ResponseEntity<JsonNode> result = this.restTemplate.exchange(tenant, HttpMethod.POST, request, JsonNode.class);
			if (enableMFAWidgetFlow) {
				Integer userId = ((TokenStore) RequestContextHolder.currentRequestAttributes().getAttribute("UserTokenStore", 1)).getUserId();
				tokenStoreRepository.deleteById(userId);
			}
			return result;
		} catch (Exception ex){
			logger.error("Exception occurred : ", ex);
			return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String CreateSession(Integer userId, String mfaToken){
		String sessionUuid = java.util.UUID.randomUUID().toString();
		tokenStoreRepository.save(new TokenStore(userId,sessionUuid,mfaToken));
		return sessionUuid;
	}

	public TokenStore GetTokenStore(String auth){
		return tokenStoreRepository.findByToken(auth);
	}

	public JsonNode CompleteLogin(AdvanceLoginRequest advanceLoginRequest, HttpServletResponse httpServletResponse) throws Exception {

		String accessToken = receiveOAuthTokenCCForUser(advanceLoginRequest);

		HttpHeaders headers = setHeaders(accessToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		String url = settingsService.getTenantURL() + "/CDirectoryService/GetUser";
		ResponseEntity<JsonNode> getResponse = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);
		JsonNode response = getResponse.getBody();
		String mfaUser = response.get("Result").get("Name").asText();

		TokenStore token =  tokenStoreRepository.findBySession(advanceLoginRequest.getSessionUuid());
		DBUser dbuser = userService.Get(token.getUserId());

		if(userService.GetMFAUserName(dbuser.getName()).equalsIgnoreCase(mfaUser)){

			token.setMfaToken(accessToken);
			tokenStoreRepository.save(token);
			ObjectNode objectNode = new ObjectMapper().createObjectNode();
			objectNode.put("Username",dbuser.getName());
			objectNode.put("DisplayName",dbuser.getDisplayName());

			Cookie cookie = new Cookie(".ASPXAUTH", accessToken);
			cookie.setHttpOnly(true);
			cookie.setSecure(true);
			cookie.setPath("/");
			httpServletResponse.addCookie(cookie);
			return objectNode;

		} else{
			throw new Exception("Invalid Tokens.Login Failed");
		}
	}

	private String receiveOAuthTokenCCForUser(AdvanceLoginRequest advanceLoginRequest) {
		try {
			String url = settingsService.getTenantURL() + "/oauth2/token/" + settingsService.getOauthApplicationID();
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Content-Type", "application/x-www-form-urlencoded");

			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("code", advanceLoginRequest.getAuthorizationCode());
			map.add("grant_type", GrantType.authorization_code.name());
			map.add("redirect_uri", this.demoAppBaseURL + ":8080/RedirectResource");
			map.add("client_id", advanceLoginRequest.getClientId());
			map.add("code_verifier", advanceLoginRequest.getCodeVerifier());

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);
			ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
			JsonNode responseResult = response.getBody();
			return responseResult.get("access_token").asText();

		} catch (Exception ex){
			logger.error("Exception occurred : ", ex);
			throw ex;
		}
    }

	public JsonNode setAuthCookie(Boolean enableMFAWidgetFlow, AdvanceLoginRequest advanceLoginRequest, HttpServletResponse httpServletResponse) throws Exception {
		try {
			String accessToken = receiveOAuthTokenCCForUser(advanceLoginRequest);
			String sessionUuid = "";
			if(enableMFAWidgetFlow){
				Integer userId = mfaUserMappingRepository.findByMfaUserId(advanceLoginRequest.getClientId()).getUserId();
				sessionUuid = CreateSession(userId, accessToken);
			}

			HttpHeaders headers = setHeaders(accessToken);
			HttpEntity<String> request = new HttpEntity<>(headers);

			String url = settingsService.getTenantURL() + "/CDirectoryService/GetUser";
			ResponseEntity<JsonNode> getResponse = restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);
			JsonNode response = getResponse.getBody();
			String mfaUsername = response.get("Result").get("Name").asText();

			Cookie cookie = new Cookie(".ASPXAUTH", accessToken);
			cookie.setHttpOnly(true);
			cookie.setSecure(true);
			cookie.setPath("/");
			httpServletResponse.addCookie(cookie);

			ObjectNode objectNode = new ObjectMapper().createObjectNode();
			objectNode.put("mfaUsername", mfaUsername);
			objectNode.put("SessionUuid", sessionUuid);
			return objectNode;
		} catch (Exception e) {
			logger.error("Exception occurred : ", e);
			throw e;
		}
	}
}
