package com.idaptive.usermanagement.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.idaptive.usermanagement.Repos.TokenStoreRepository;
import com.idaptive.usermanagement.Repos.UserRepository;
import com.idaptive.usermanagement.entity.DBUser;
import com.idaptive.usermanagement.entity.TokenStore;
import com.idaptive.usermanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.context.request.RequestContextHolder;

@Service
@RefreshScope
public class UserOpsService {

	Logger logger = LoggerFactory.getLogger(UserOpsService.class);

	@Value("${customerId}")
	private String tenantID;

	@Value("${tenant}")
	private String tenant;

	@Value("${oauthAppId}")
	private String applicationID;

	@Value("${oauthUser}")
	private String clientID;

	@Value("${oauthPassword}")
	private String clientSecret;

	@Value("${scope}")
	private String scope;

	@Value("${grantType}")
	private String grantType;

	@Value("${mfaRole}")
	private String roleName;

	@Autowired
	private UserRepository repo;

	@Autowired
	private TokenStoreRepository tokenStoreRepository;

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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private String receiveOAuthTokenCC() {
		ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
		details.setAccessTokenUri(tenant + "/oauth2/token/" + applicationID);
		details.setClientId(clientID);
		details.setClientSecret(clientSecret);
		details.setScope(Arrays.asList(scope));
		details.setGrantType(grantType);
		OAuth2RestTemplate template = new OAuth2RestTemplate(details);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		OAuth2AccessToken token = template.getAccessToken();
		return token.getValue();
	}

	private HttpHeaders setHeaders(String token) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set("x-centrify-native-client", "true");
		httpHeaders.set("content-type", "application/json");
		httpHeaders.set("cache-control", "no-cache");
		httpHeaders.set("Authorization", "Bearer " + token);
		return httpHeaders;
	}

	private HttpHeaders prepareForRequestOauth() {
		String token = receiveOAuthTokenCC();
		return setHeaders(token);
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
		String updateUserUrl = tenant + "/CDirectoryService/ChangeUser";
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
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	//Get user information using specified UUID
	public ResponseEntity<JsonNode> getUser(String uuid, String token) {
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>("{\"ID\":\"" + uuid + "\"}", headers);
		String url = tenant + "/CDirectoryService/GetUser";
		JsonNode response = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class).getBody();
		JsonNode result = response.get("Result");
		String name = response.get("Result").get("Name").asText();
		String[] nameArr = name.split("@");
		ObjectNode objNode = (ObjectNode) result;
		objNode.remove("Name");
		objNode.put("Name", nameArr[0]);
		objNode.put(roleName, isRolePresent(uuid, token));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	
	//This method developed for fetching federated user information with userId
	public ResponseEntity<JsonNode> getUserInfo(String userId, String token) {
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>(headers);
		String url = tenant + "/UserMgmt/GetUserInfo?ID=" + userId;
		return restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
	}

	static ArrayList<String> iconList = new ArrayList<>();

	
	//This method returns icon url and appkey using that client can fech the icon images.
	public ResponseEntity<JsonNode> userDashboard(String username, String force, String token) {
		HashMap<String, ArrayList<HashMap<String, String>>> appInfo = new HashMap<>();
		String url = tenant + "/UPRest/GetUPData" + "?" + "force=" + force + "&username=" + username;
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>(headers);
		JsonNode result = restTemplate.postForObject(url, request, JsonNode.class);
		JsonNode arrNode = result.get("Result").get("Apps");
		if (arrNode.isArray()) {
			for (final JsonNode objNode : arrNode) {
				ArrayList<HashMap<String, String>> appList = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> map = new HashMap<>();
				map.put("Icon", objNode.get("Icon").asText());
				iconList.add(objNode.get("Icon").asText());
				map.put("AppKey", objNode.get("AppKey").asText());
				appList.add(map);
				appInfo.put(objNode.get("Name").asText(), appList);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode response = mapper.convertValue(appInfo, JsonNode.class);
		return new ResponseEntity<JsonNode>(response, HttpStatus.OK);
	}

	public String GetMFAUserName(String name){
		return name + "@" + this.tenantID;
	}

//	//Update configuration files of all services.This method calls config-server URL.
//	public ResponseEntity<JsonNode> updateConfig(JsonNode body) {
//		String plainCreds = configUsername + ":" + configPassword;
//		byte[] plainCredsBytes = plainCreds.getBytes();
//		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
//		String base64Creds = new String(base64CredsBytes);
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Authorization", "Basic " + base64Creds);
//		HttpEntity request = new HttpEntity(body, headers);
//		Application application = eurekaClient.getApplication("config-server");
//		String url = "http://" + application.getInstances().get(0).getIPAddr() + ":"
//				+ application.getInstances().get(0).getPort() + "/updateconfig";
//		return restTemplate.exchange(url, HttpMethod.PUT, request, JsonNode.class);
//	}

	
	//Get configuration properties.This method calls config-server URL.
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
//				+ application.getInstances().get(0).getPort() + "/getconfig";
//		return restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class);
//	}

	// This method checks whether role is assigned to user mentioned in
	// configuration file
	public boolean isRolePresent(String uuid, String token) {
		String url = tenant + "/UserMgmt/GetUsersRolesAndAdministrativeRights?id=" + uuid;
		HttpHeaders headers = prepareForRequest(token);
		HttpEntity<String> request = new HttpEntity<>(headers);
		JsonNode result = restTemplate.postForObject(url, request, JsonNode.class);
		JsonNode arr = result.get("Result").get("Results");
		for (JsonNode jsonNode : arr) {
			if (jsonNode.get("Row").get("RoleName").asText().equals(roleName)) {
				return true;
			}
		}
		return false;
	}
}
