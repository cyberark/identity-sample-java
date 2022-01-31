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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sampleapp.entity.Response;
import com.sampleapp.entity.User;
import com.sampleapp.entity.VerifyTotpReq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sampleapp.service.UserOpsService;
import com.sampleapp.config.AuthFilter;

@RestController
public class UserOpsController {

	@Autowired
	private UserOpsService userOpsService;

	@PutMapping("/userops/{uuid}")
	public ResponseEntity<JsonNode> updateUser(HttpServletRequest request, HttpServletResponse httpServletResponse, @RequestBody User user,
											   @PathVariable String uuid) throws Exception {
		Boolean enableMFAWidgetFlow = AuthFilter.readServletCookie(request,"flow").get().equals("flow3");
		String token = AuthFilter.findCookie(request, ".ASPXAUTH");
		if (token != null) {
			try {
				AuthFilter.checkHeartBeat(request, httpServletResponse, token);
			} catch (Exception ex) {
				return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.FORBIDDEN);
			}
			return userOpsService.updateUser(token, uuid, user, enableMFAWidgetFlow);
		}
		return new ResponseEntity(new Response(false, "User Session Ended. Please login again to proceed."), HttpStatus.FORBIDDEN);
	}

	@GetMapping("/userops/{uuid}")
	public ResponseEntity<JsonNode> getUser(HttpServletRequest request, HttpServletResponse httpServletResponse, @PathVariable String uuid) throws Exception{
		String token = AuthFilter.findCookie(request, ".ASPXAUTH");
		if (token != null){
			try {
				AuthFilter.checkHeartBeat(request, httpServletResponse, token);
			} catch (Exception ex) {
				return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.FORBIDDEN);
			}
			return userOpsService.getUser(uuid, token);
		}
		return new ResponseEntity(new Response(false, "User Session Ended. Please login again to proceed."), HttpStatus.FORBIDDEN);
	}

	@GetMapping("/userops/getTotpQR")
	public ResponseEntity<JsonNode> getTotpQR(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
		String token = AuthFilter.findCookie(request, ".ASPXAUTH");
		if (token != null){
			try {
				AuthFilter.checkHeartBeat(request, httpServletResponse, token);
			} catch (Exception ex) {
				return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.FORBIDDEN);
			}
			return userOpsService.getTotpQR(token);
		}
		return new ResponseEntity(new Response(false, "User Session Ended. Please login again to proceed."), HttpStatus.FORBIDDEN);
	}

	@PostMapping("/userops/verifyTotp")
	public ResponseEntity<JsonNode> verifyTotp(HttpServletRequest request, HttpServletResponse httpServletResponse, @RequestBody VerifyTotpReq req) throws Exception {
		String token = AuthFilter.findCookie(request, ".ASPXAUTH");
		if (token != null){
			try {
				AuthFilter.checkHeartBeat(request, httpServletResponse, token);
			} catch (Exception ex) {
				return new ResponseEntity(new Response(false, ex.getMessage()), HttpStatus.FORBIDDEN);
			}
			return userOpsService.verifyTotp(token, req);
		}
		return new ResponseEntity(new Response(false, "User Session Ended. Please login again to proceed."), HttpStatus.FORBIDDEN);
	}

	@GetMapping("/userops/challengeID")
	public ResponseEntity<JsonNode> getChallengeID(HttpServletRequest request) throws Exception {
		String token = AuthFilter.findCookie(request, ".ASPXAUTH");
		if (token != null){
			return userOpsService.getChallengeID(token);
		}
		return new ResponseEntity(new Response(false, "User Session Ended. Please login again to proceed."), HttpStatus.FORBIDDEN);
	}
}
