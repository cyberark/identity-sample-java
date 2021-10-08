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

import com.sampleapp.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.sampleapp.service.SettingsService;
import java.io.IOException;

@RestController
public class SettingsController {

	@Autowired
	private SettingsService settingsService;

	private final Logger logger = LoggerFactory.getLogger(SettingsController.class);

	@PutMapping("updateSettings")
	public ResponseEntity<JsonNode> updateSettings(@RequestBody JsonNode body, HttpServletRequest request) throws IOException {
		Response response = new Response();
		try{
			settingsService.updateSettings(body);
			response.Result = "Settings updated successfully";
			return new ResponseEntity(response, HttpStatus.OK);
		}catch (Exception e){
			logger.error("Exception occurred : ", e);
			response.Success = false;
			response.ErrorMessage = "Settings not updated.";
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("getSettings")
	public ResponseEntity<JsonNode> getSettings() {
		Response response = new Response();
		try{
			response.Result =  settingsService.getSettings();
			return new ResponseEntity(response, HttpStatus.OK);
		}catch (Exception ex){
			logger.error("Exception occurred : ", ex);
			response.Success = false;
			response.ErrorMessage = "Failed to fetch settings.";
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
