package com.idaptive.usermanagement.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idaptive.usermanagement.entity.*;
import com.idaptive.usermanagement.service.AuthService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.idaptive.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

@RestController
public class AuthController {

	@Autowired
	private AuthService authService;
	@Autowired
	private UserService userService;
	private final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("auth/beginAuth")
	public ResponseEntity<JsonNode> beginAuth(@RequestBody AuthRequest authRequest,HttpServletResponse response) {
		logger.info("AuthRequest is as follow: -");
		try {
			return this.authService.startAuthenticationWithObject(authRequest,response);
		} catch (JsonProcessingException e) {
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@PostMapping("auth/advanceAuth")
	public ResponseEntity<JsonNode> advanceAuth(@RequestBody JsonNode advAuthRequest,HttpServletResponse response) {
		return this.authService.advanceAuthenticationByObject(advAuthRequest,response);
	}

	@PostMapping("auth/out")
	public ResponseEntity<JsonNode> logout(HttpServletRequest request,HttpServletResponse response) {
		Cookie[] cookieArray = request.getCookies();
		for (Cookie cookie : cookieArray) {
			if (cookie.getName().equals(".ASPXAUTH")) {
				return this.authService.logout(cookie.getValue(),response);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
	
	@GetMapping({ "auth/socialLogin/{idpName}" })
	public ResponseEntity<JsonNode> socialLogin(@PathVariable String idpName) {	
		return this.authService.socialLogin(idpName);
	}

	@GetMapping({ "auth/socialLogin" })
	public ResponseEntity<JsonNode> socialLoginResult(@RequestParam String ExtIdpAuthChallengeState,
			@RequestParam String username, @RequestParam String customerId, HttpServletResponse httpServletResponse) {
		return authService.socialLoginResult(ExtIdpAuthChallengeState, username, customerId,httpServletResponse);
	}

	@PostMapping({ "/BasicLogin" })
	public ResponseEntity<JsonNode> BasicLogin(@RequestBody BasicLoginRequest request, HttpServletResponse httpServletResponse) {
		Response response = new Response();
		try {
			DBUser user = userService.Get(request.getUsername(), request.getPassword());
			if (user != null) {
				String sessionUuid = authService.CreateSession(user.getId());
				ObjectMapper objectMapper = new ObjectMapper();
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put("SessionUuid",sessionUuid);
				objectNode.put("MFAUserName",user.getName());
				response.Result = objectNode;
			}else{
				response.Success = false;
				response.ErrorMessage = "Invalid Username or Password";
			}
			return new ResponseEntity(response, (user != null ? HttpStatus.OK : HttpStatus.NOT_FOUND));
		}catch (Exception ex){
			response.Success = false;
			response.ErrorMessage = ex.getMessage();
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping({ "/CompleteLogin" })
	public ResponseEntity<Response> CompleteLogin(@RequestBody AdvanceLoginRequest advanceLoginRequest, HttpServletResponse httpServletResponse){
		Response response = new Response();
		try {
			response.Result =authService.CompleteLogin(advanceLoginRequest,httpServletResponse);
			return new ResponseEntity(response, HttpStatus.OK);
		}catch (Exception ex){
			response.Success = false;
			response.ErrorMessage = ex.getMessage();
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/RedirectResource")
	public ResponseEntity<Response> RedirectResource(HttpServletRequest request, HttpServletResponse httpServletResponse) {
		Response response = new Response();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode objectNode = objectMapper.createObjectNode();
		objectNode.put("AuthorizationCode", request.getParameterMap().get("code")[0]);
		response.Result = objectNode;
		return new ResponseEntity(response, HttpStatus.OK);
	}
}