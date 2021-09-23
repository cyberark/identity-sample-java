package com.idaptive.usermanagement.controller;

import javax.servlet.http.HttpServletRequest;
import com.idaptive.usermanagement.config.AuthFilter;
import com.idaptive.usermanagement.entity.UserRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.idaptive.usermanagement.service.UserService;

@RestController
public class UsersController {
	
	@Autowired
	private UserService userService;

	@PostMapping("/user/register")
	public ResponseEntity<JsonNode> createUser(HttpServletRequest request, @RequestBody UserRegistration userRegistration) throws Exception {
		Boolean enableMFAWidgetFlow = AuthFilter.readServletCookie(request,"flow").get().equals("flow2");
		return userService.createUser(userRegistration.getUser(), userRegistration.getIsMfa(), enableMFAWidgetFlow);
	}
}
