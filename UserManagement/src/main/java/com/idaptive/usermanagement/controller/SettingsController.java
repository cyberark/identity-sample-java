package com.idaptive.usermanagement.controller;

import javax.servlet.http.HttpServletRequest;

import com.idaptive.usermanagement.entity.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.idaptive.usermanagement.service.SettingsService;
import java.io.IOException;

@RestController
public class SettingsController {

	@Autowired
	private SettingsService settingsService;

	@PutMapping("updateSettings")
	public ResponseEntity<JsonNode> updateSettings(@RequestBody JsonNode body, HttpServletRequest request) throws IOException {
		Response response = new Response();
		try{
			settingsService.updateSettings(body);
			response.Result = "Settings updated successfully";
			return new ResponseEntity(response, HttpStatus.OK);
		}catch (Exception e){
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
			response.Success = false;
			response.ErrorMessage = "Failed to fetch settings.";
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
