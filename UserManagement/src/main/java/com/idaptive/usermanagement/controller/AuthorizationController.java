package com.idaptive.usermanagement.controller;

import com.cyberark.entities.TokenHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idaptive.usermanagement.entity.*;
import com.idaptive.usermanagement.service.BaseAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.EnumMap;

import com.idaptive.usermanagement.config.AuthFilter;

@RestController
public class AuthorizationController {

    private final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    @Autowired
    public AuthFlows authFlows;

    @GetMapping("pkceMetaData")
    public ResponseEntity<JsonNode> getPKCEMetaData() {

        logger.info("Get PKCE Metadata");
        Response response = new Response();
        try
        {
            PKCEMetaData pkceMetaData = this.authFlows.getEnumMap().get(AuthorizationFlow.OAUTH).getPKCEMetaData();
            response.Result = pkceMetaData;
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("buildAuthorizeURL")
    public ResponseEntity<JsonNode> buildAuthorizeURL(@RequestBody AuthorizationMetadataRequest metadataRequest) {

        logger.info("Invoking Get AuthorizeUrl Request");
        Response response = new Response();
        try
        {
            String authorizeUrl = this.authFlows.getEnumMap().get(metadataRequest.authFlow).buildAuthorizeURL(metadataRequest);

            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("authorizeUrl", authorizeUrl);
            response.Result = objectNode;

            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (IOException ex) {
            logger.error("Exception at buildAuthorizeURL() : ", ex);
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("tokenSet")
    public ResponseEntity<JsonNode> getTokenSet(@RequestBody TokenMetadataRequest metadataRequest) {

        logger.info("Get Token Set");
        Response response = new Response();
        TokenHolder tokenHolder;
        EnumMap<AuthorizationFlow, BaseAuthorizationService> serviceEnumMap = this.authFlows.getEnumMap();
        try
        {
            switch (metadataRequest.grantType)
            {
                case authorization_code:
                    tokenHolder = serviceEnumMap.get(metadataRequest.authFlow).getTokenSet(metadataRequest);
                    break;
                case client_credentials:
                    tokenHolder = serviceEnumMap.get(AuthorizationFlow.OAUTH).getTokenSetWithClientCreds(metadataRequest);
                    break;
                case password:
                    tokenHolder = serviceEnumMap.get(AuthorizationFlow.OAUTH).getTokenSetWithPassword(metadataRequest);
                    break;
                default:
                    throw new Exception("Invalid Grant type is sent");
            }
            response.Result = tokenHolder;
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("tokenRequestPreview")
    public ResponseEntity<JsonNode> tokenRequestPreview(@RequestBody TokenMetadataRequest metadataRequest) {

        logger.info("Get tokenRequestPreview");
        Response response = new Response();
        try
        {
            response.Result = this.authFlows.getEnumMap().get(metadataRequest.authFlow).tokenRequestPreview(metadataRequest);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("claims")
    public ResponseEntity<JsonNode> claims(@RequestParam String token) throws IOException {

        logger.info("Get Claims");
        Response response = new Response();
        try
        {
            String tokenString = AuthFilter.cleanIt(token);
            response.Result = this.authFlows.getEnumMap().get(AuthorizationFlow.OIDC).getClaims(tokenString);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("oidc/userInfo")
    public ResponseEntity<JsonNode> userInfo(@RequestParam String accessToken) throws IOException {

        logger.info("Get UserInfo");
        Response response = new Response();
        try
        {
            String accessTokenString = AuthFilter.cleanIt(accessToken);
            response.Result = this.authFlows.getEnumMap().get(AuthorizationFlow.OIDC).getUserInfo(accessTokenString);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
