package com.idaptive.usermanagement.service;

import java.io.IOException;
import java.util.Arrays;

import com.cyberark.entities.TokenHolder;
import com.idaptive.usermanagement.Repos.MfaUserMappingRepository;
import com.idaptive.usermanagement.Repos.UserRepository;
import com.idaptive.usermanagement.entity.*;
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
import com.idaptive.usermanagement.entity.User;
import com.idaptive.usermanagement.exception.RoleNotFoundException;
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
			String json =  mapper.writeValueAsString(user);
			user.setName(name);
			return json;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String receiveOAuthTokenForClientCreds() throws Exception {
		TokenMetadataRequest metadataRequest = new TokenMetadataRequest();
		metadataRequest.grantType = GrantType.client_credentials;

		TokenHolder tokenHolder = null;
		try {
			tokenHolder = this.authFlows.getEnumMap().get(AuthorizationFlow.OAUTH).getTokenSetWithClientCreds(metadataRequest);
		} catch (IOException e) {
			throw new Exception("Error occurred while fetching access_token");
		}

		return tokenHolder.getAccessToken();
	}

	private HttpHeaders setHeaders(String token) {
		HttpServletRequest currentRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		HttpHeaders httpHeaders = new HttpHeaders();
		
		httpHeaders.set("X-IDAP-NATIVE-CLIENT", "true");
		httpHeaders.set("content-type", "application/json");
		httpHeaders.set("cache-control", "no-cache");
		httpHeaders.set("Authorization", "Bearer " + token);
		httpHeaders.set("X_FORWARDED_FOR", currentRequest.getHeader("CLIENT_IP"));
		return httpHeaders;
	}

	private HttpHeaders prepareForRequestOauth()  throws Exception {
		String token = receiveOAuthTokenForClientCreds();
		return setHeaders(token);
	}

	public ResponseEntity<JsonNode> createUser(User user, boolean isMfa, boolean enableMFAWidgetFlow) throws Exception{
		String userJson = "";
		try {
			userJson = getJson(user);
			HttpHeaders headers = prepareForRequestOauth();
			HttpEntity<String> createuserrequest = new HttpEntity<>(userJson, headers);
			String createUserUrl = settingsService.getTenantURL() + "/CDirectoryService/Signup";
			String updateRoleUrl = settingsService.getTenantURL() + "/Roles/UpdateRole";
			ResponseEntity<JsonNode> createUserResponse = null;
			createUserResponse = restTemplate.exchange(createUserUrl, HttpMethod.POST, createuserrequest,
					JsonNode.class);
			StringBuffer message = new StringBuffer("User name " + user.getName() + " is already in use.");
			if (createUserResponse.getBody().get("Result").isNull()) {
				if (createUserResponse.getBody().get("Message").asText().contentEquals(message)) {
					JsonNode createUserResponseBody = createUserResponse.getBody();
					ObjectNode objNode = (ObjectNode) createUserResponseBody;
					objNode.remove("Message");
					objNode.put("Message", "User name " + user.getName() + " is already in use.");
					return createUserResponse;
				}
			} else {
				if (isMfa) {
					String roleUuid = getRoleUuid(settingsService.getRoleName());

					HttpEntity<String> updateRoleRequest = new HttpEntity<>(
							"{\"Users\":{\"Add\":[\"" + createUserResponse.getBody().get("Result").get("UserId").asText()
									+ "\"]},\"Name\":\"" + roleUuid + "\",\"Description\":\"\"}",
							headers);
					restTemplate.exchange(updateRoleUrl, HttpMethod.POST, updateRoleRequest, JsonNode.class);
				}
				if(enableMFAWidgetFlow) {
					saveToCustomDb(user, createUserResponse.getBody().get("Result").get("UserId").asText());
				}

				return createUserResponse;
			}

			Arrays.fill(user.password, ' ');

			return createUserResponse;
		} catch (JsonProcessingException e) {
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (RoleNotFoundException e) {
			return new ResponseEntity<JsonNode>(e.exceptionBody(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	public void saveToCustomDb(User user, String mfaUserId){
		DBUser outUser = repo.save(user.getUser());
		mfaUserMappingRepository.save(new MfaUserMapping(outUser.getId(),mfaUserId));
	}

	public String getRoleUuid(String roleName) throws RoleNotFoundException, Exception {
		String getRoles = settingsService.getTenantURL() + "/Redrock/query";
		HttpHeaders headers = prepareForRequestOauth();
		HttpEntity<String> getRolesRequest = new HttpEntity<>(
				"{ Script: \"Select * from Role WHERE Name = \'" + roleName + "\' ORDER BY Name COLLATE NOCASE \"}",
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
			return  dbUser;
		}catch(Exception ex){
			logger.error(ex.getMessage(),ex);
			return  null;
		}
	}
	public DBUser Get(Integer id) {
		return repo.findById(id).get();
	}

	public String GetMFAUserName(String name){
		return name + "@" + settingsService.getLoginSuffix();
	}
}
