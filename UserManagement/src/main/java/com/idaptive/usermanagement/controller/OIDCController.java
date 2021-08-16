package com.idaptive.usermanagement.controller;

import com.cyberark.entities.TokenHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idaptive.usermanagement.entity.PKCEMetaData;
import com.idaptive.usermanagement.entity.Response;
import com.idaptive.usermanagement.entity.TokenMetadataRequest;
import com.idaptive.usermanagement.service.OIDCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import com.idaptive.usermanagement.config.AuthFilter;

@RestController
public class OIDCController {

    @Autowired
    private OIDCService oidcService;

    private final Logger logger = LoggerFactory.getLogger(OIDCController.class);

    @GetMapping("oidc/pkceMetaData")
    public ResponseEntity<JsonNode> getPKCEMetaData() {

        logger.info("Get PKCE Metadata");
        Response response = new Response();
        try
        {
            PKCEMetaData pkceMetaData = this.oidcService.getPKCEMetaData();
            response.Result = pkceMetaData;
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("oidc/buildAuthorizeURL")
    public ResponseEntity<JsonNode> buildAuthorizeURL(@RequestParam String codeChallenge, @RequestParam String responseType) {

        logger.info("Invoking Get AuthorizeUrl Request");
        Response response = new Response();
        try
        {
            String codeChallengeString = AuthFilter.cleanIt(codeChallenge);
            String authorizeUrl = this.oidcService.buildAuthorizeURL(codeChallengeString, responseType);
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("authorizeUrl", authorizeUrl);
            response.Result = objectNode;

            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (IOException ex) {
            logger.error("Exception at buildAuthorizeURL() : ", ex);
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("oidc/buildImplicitAuthURL")
    public ResponseEntity<JsonNode> buildImplicitAuthURL(@RequestParam String responseType) {

        logger.info("Invoking Get Implicit AuthorizeUrl Request");
        Response response = new Response();
        try
        {
            String responseTypeString = AuthFilter.cleanIt(responseType);
            String authorizeUrl = this.oidcService.buildImplicitAuthURL(responseTypeString);
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("authorizeUrl", authorizeUrl);
            response.Result = objectNode;

            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (IOException ex) {
            logger.error("Exception at buildImplicitAuthURL() : ", ex);
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("oidc/tokenSet")
    public ResponseEntity<JsonNode> getTokenSet(@RequestBody TokenMetadataRequest tokenMetadataRequest) {

        logger.info("Get Token Set");
        Response response = new Response();
        try
        {
            TokenHolder tokenHolder = this.oidcService.getTokenSet(tokenMetadataRequest);
            response.Result = tokenHolder;
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("oidc/claims")
    public ResponseEntity<JsonNode> claims(@RequestParam String idToken) throws IOException {

        logger.info("Get Claims");
        Response response = new Response();
        try
        {
            String idTokenString = AuthFilter.cleanIt(idToken);
            response.Result = this.oidcService.getClaims(idTokenString);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("oidc/userInfo")
    public ResponseEntity<JsonNode> userInfo(@RequestParam String accessToken) throws IOException {

        logger.info("Get UserInfo");
        Response response = new Response();
        try
        {
            String accessTokenString = AuthFilter.cleanIt(accessToken);
            response.Result = this.oidcService.getUserInfo(accessTokenString);
            return new ResponseEntity(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            response.Success = false;
            response.ErrorMessage = ex.getMessage();
            return new ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
