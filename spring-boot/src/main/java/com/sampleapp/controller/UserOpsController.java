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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.sampleapp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sampleapp.service.UserOpsService;
import com.sampleapp.config.AuthFilter;

@RestController
public class UserOpsController {

	@Autowired
	private UserOpsService userOpsService;

	@PutMapping("/userops/{uuid}")
	public ResponseEntity<JsonNode> updateUser(HttpServletRequest request, @RequestBody User user,
			@PathVariable String uuid) throws JsonProcessingException {
		Boolean enableMFAWidgetFlow = AuthFilter.readServletCookie(request,"flow").get().equals("flow2");	
		Cookie[] cookieArray = request.getCookies();
		for (Cookie cookie : cookieArray) {
			if (cookie.getName().equals(".ASPXAUTH")) {
				return userOpsService.updateUser(cookie.getValue(), uuid, user, enableMFAWidgetFlow);
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}

	@GetMapping("/userops/{uuid}")
	public ResponseEntity<JsonNode> getUser(HttpServletRequest request, @PathVariable String uuid) {
		Cookie[] cookieArray = request.getCookies();
		for (Cookie cookie : cookieArray) {
			if (cookie.getName().equals(".ASPXAUTH")) {
				return userOpsService.getUser(uuid, cookie.getValue());
			}
		}
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}
}