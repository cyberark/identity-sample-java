package com.idaptive.usermanagement.service;

import java.io.IOException;

import com.cyberark.entities.TokenHolder;
import com.idaptive.usermanagement.Repos.MfaUserMappingRepository;
import com.idaptive.usermanagement.Repos.UserRepository;
import com.idaptive.usermanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
//import com.netflix.discovery.EurekaClient;
//import com.netflix.discovery.shared.Application;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

@Service
@RefreshScope
public class UserService {

	Logger logger = LoggerFactory.getLogger(UserService.class);

	@Value("${customerId}")
	private String tenantID;

	@Value("${tenant}")
	private String tenant;

	@Value("${mfaRole}")
	private String roleName;

//
//	@Value("${spring.cloud.config.username}")
//	private String configUsername;
//
//	@Value("${spring.cloud.config.password}")
//	private String configPassword;
//
//	@Autowired
//	private EurekaClient eurekaClient;

	@LoadBalanced
	private final RestTemplate restTemplate;

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
			String createUserUrl = tenant + "/CDirectoryService/Signup";
			String updateRoleUrl = tenant + "/Roles/UpdateRole";
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
					String roleUuid = getRoleUuid(roleName);

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
		String getRoles = tenant + "/Redrock/query";
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

	public DBUser Get(String name, String password) {
		DBUser exampleUser = new DBUser();
		exampleUser.setName(name);
		exampleUser.setPassword(password);
		try {
			return repo.findOne(Example.of(exampleUser)).get();
		}catch(Exception ex){
			logger.error(ex.getMessage(),ex);
			return  null;
		}
	}
	public DBUser Get(Integer id) {
		return repo.findById(id).get();
	}

	public String GetMFAUserName(String name){
		return name + "@" + this.tenantID;
	}

//	public ResponseEntity<JsonNode> getConfig() {
//		String plainCreds = configUsername + ":" + configPassword;
//		byte[] plainCredsBytes = plainCreds.getBytes();
//		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
//		String base64Creds = new String(base64CredsBytes);
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Authorization", "Basic " + base64Creds);
//		HttpEntity request = new HttpEntity(headers);
//		Application application = eurekaClient.getApplication("config-server");
//		String url = "http://" + application.getInstances().get(0).getIPAddr() + ":"
//				+ application.getInstances().get(0).getPort() + "/getclientconfig";
//		return restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);
//
//	}
//
//	public ResponseEntity<JsonNode> refreshConfig() {
//		HttpHeaders headers = new HttpHeaders();
//		HttpEntity request = new HttpEntity(headers);
//		Application userOpsApplication = eurekaClient.getApplication("user-ops-service");
//		Application authApplication = eurekaClient.getApplication("auth-service");
//
//		String userOpsUrl = "http://" + userOpsApplication.getInstances().get(0).getIPAddr() + ":"
//				+ userOpsApplication.getInstances().get(0).getPort() + "/actuator/refresh";
//		String authUrl = "http://" + authApplication.getInstances().get(0).getIPAddr() + ":"
//				+ authApplication.getInstances().get(0).getPort() + "/actuator/refresh";
//		restTemplate.exchange(userOpsUrl, HttpMethod.POST, request, JsonNode.class);
//		return restTemplate.exchange(authUrl, HttpMethod.POST, request, JsonNode.class);
//
//	}


}
