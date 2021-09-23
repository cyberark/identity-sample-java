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

package com.sampleapp.controller;

import com.sampleapp.entity.BasicLoginRequest;
import com.sampleapp.entity.AdvanceLoginRequest;
import com.sampleapp.entity.AuthRequest;
import com.sampleapp.entity.DBUser;
import com.sampleapp.entity.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sampleapp.config.AuthFilter;
import com.sampleapp.service.AuthService;
import com.sampleapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@RestController
public class AuthController {

	@Autowired
	private AuthService authService;
	@Autowired
	private UserService userService;
	private final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("auth/beginAuth")
	public ResponseEntity<JsonNode> beginAuth(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
		logger.info("AuthRequest received");
		try {
			Boolean enableMFAWidgetFlow = false; //flow1 endpoint so setting to false
			return this.authService.startAuthenticationWithObject(authRequest,response, enableMFAWidgetFlow);
		} catch (Exception e) {
			logger.error("Exception occurred : ", e);
			return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("auth/advanceAuth")
	public ResponseEntity<JsonNode> advanceAuth(@RequestBody JsonNode advAuthRequest,HttpServletResponse response) throws UnsupportedEncodingException {
		return this.authService.advanceAuthenticationByObject(advAuthRequest,response);
	}

	@PostMapping("auth/out")
	public ResponseEntity<JsonNode> logout(HttpServletRequest request,HttpServletResponse response) {
		Cookie[] cookieArray = request.getCookies();
		Boolean enableMFAWidgetFlow = AuthFilter.readServletCookie(request,"flow").get().equals("flow2");
		String authToken = "";
		for (Cookie cookie : cookieArray) {
			if (cookie.getName().equals(".ASPXAUTH")) {
				authToken = cookie.getValue();
			}
		}
		return this.authService.logout(authToken, response, enableMFAWidgetFlow);
	}
	
	@PostMapping({ "/BasicLogin" })
	public ResponseEntity<JsonNode> BasicLogin(@RequestBody BasicLoginRequest request, HttpServletResponse httpServletResponse) {
		Response response = new Response();
		try {
			DBUser user = userService.Get(request.getUsername(), request.Password);
			Arrays.fill(request.Password, ' ');

			if (user != null) {
				String sessionUuid = authService.CreateSession(user.getId());
				ObjectMapper objectMapper = new ObjectMapper();
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put("SessionUuid",sessionUuid);
				objectNode.put("MFAUserName",userService.GetMFAUserName(user.getName()));
				response.Result = objectNode;
			}else{
				response.Success = false;
				response.ErrorMessage = "Invalid Username or Password";
			}
			HttpStatus httpStatus = HttpStatus.NOT_FOUND;
			if(user != null) httpStatus = HttpStatus.OK;

			return new ResponseEntity(response, httpStatus);
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
		try {
			if (request.getParameter("error") != null){
				throw new Exception(request.getParameter("error_description"));
			}
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("AuthorizationCode", request.getParameterMap().get("code")[0]);
			response.Result = objectNode;
			return new ResponseEntity(response, HttpStatus.OK);
		}catch(Exception ex){
			response.Success = false;
			response.ErrorMessage = ex.getMessage();
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}