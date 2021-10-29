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

package com.sampleapp.service;

import com.cyberark.client.AuthorizeUrlBuilder;
import com.cyberark.client.OAuthClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.cyberark.exception.IdentityException;
import com.sampleapp.entity.AuthorizationFlow;
import com.sampleapp.entity.AuthorizationMetadataRequest;
import com.sampleapp.entity.PKCEMetaData;
import com.sampleapp.entity.TokenMetadataRequest;
import com.sampleapp.entity.TokenRequestPreview;
import com.cyberark.requestBuilders.TokenRequest;
import com.cyberark.utils.PKCEUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
public abstract class BaseAuthorizationService<T extends OAuthClient> {

    private final Logger logger = LoggerFactory.getLogger(BaseAuthorizationService.class);

    @Value("${demoAppBaseURL}")
    public String demoAppBaseURL;

    @Value("${frontendServerPort}")
    public String frontendServerPort;

    @Autowired
    SettingsService settingsService;

    public abstract AuthorizationFlow supportedAuthorizationFlow();

    public abstract T getClient(String clientId, char[] clientSecret) throws IOException;

    public abstract UserInfo getUserInfo(String accessToken) throws IOException;

    public abstract TokenHolder getTokenSetWithClientCreds(TokenMetadataRequest tokenMetadataRequest) throws IOException;

    public abstract TokenHolder getTokenSetWithPassword(TokenMetadataRequest tokenMetadataRequest) throws IOException;

    protected abstract String getAppId();

    protected abstract String getClientId(String clientId);

    protected abstract char[] getClientSecret(char[] clientSecret);

    protected abstract String getScopesSupported();

    private final String codeChallengeMethod = "S256";

    private String getAuthorizationRedirectURL(){
        return this.demoAppBaseURL + ":" + this.frontendServerPort + "/RedirectResource";
    }

    public BaseAuthorizationService() { }

    /**
     *  Builds the /authorize URL using CyberArkIdentityAuth client
     *  @param metadataRequest Request Metadata
     *  @return authorize URL string
     */
    public String buildAuthorizeURL(AuthorizationMetadataRequest metadataRequest) throws IOException {
        try {
            AuthorizeUrlBuilder authorizeUrlBuilder = this.getClient(metadataRequest.clientId, metadataRequest.clientSecret)
                    .authorizeUrl(this.getAuthorizationRedirectURL())
                    .setResponseType(metadataRequest.responseType)
                    .setScope(this.getScopesSupported());

            if (metadataRequest.codeChallenge != null) {
                authorizeUrlBuilder.setCodeChallenge(metadataRequest.codeChallenge)
                    .setCodeChallengeMethod(codeChallengeMethod);
            }
            return authorizeUrlBuilder.build();
        }
        catch (Exception ex){
            logger.error("Exception at buildAuthorizeURL() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get Tokens using CyberArkIdentityOAuthClient client
     *  @param tokenMetadataRequest Input parameter
     *  @return TokenHolder An Object that holds access_token, id_token, refresh_token, token_type, scope, expires_in
     */
    public TokenHolder getTokenSet(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        TokenHolder tokenHolder;
        try {
            TokenRequest tokenRequest = this.getClient(tokenMetadataRequest.clientId, tokenMetadataRequest.clientSec)
                    .requestToken(tokenMetadataRequest.authorizationCode, this.getAuthorizationRedirectURL())
                    .setGrantType(tokenMetadataRequest.grantType.name());

            if (tokenMetadataRequest.codeVerifier != null){
                tokenRequest.setCodeVerifier(tokenMetadataRequest.codeVerifier);
            }
            tokenHolder = tokenRequest.execute();
            return tokenHolder;
        }
        catch (IdentityException ex) {
            logger.error("Exception at getTokenSet() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get PKCEMetaData which holds PKCEMetaData and CodeChallenge using CyberArkIdentityAuth client
     *  @return PKCEMetaData An Object that holds PKCEMetaData and CodeChallenge
     */
    public PKCEMetaData getPKCEMetaData() throws Exception {
        try {
            PKCEMetaData pkceMetaData = new PKCEMetaData();
            pkceMetaData.codeVerifier = PKCEUtil.generateCodeVerifier();
            pkceMetaData.codeChallenge = PKCEUtil.generateCodeChallenge(pkceMetaData.codeVerifier);
            return pkceMetaData;
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException ex){
            logger.error("Exception occurred at getPKCEMetaData() : ", ex);
            throw new Exception("Unable to get PKCE Metadata");
        }
    }

    /**
     *  Get Claims using CyberArkIdentityOAuthClient
     *  @param token A token in JWT Format
     *  @return JsonNode An Object that holds base64 decoded token as claims
     */
    public JsonNode getClaims(String token) throws IOException {
        JsonNode claims;
        try {
            claims = OAuthClient.claims(token);
            return claims;
        }
        catch (IdentityException ex) {
            logger.error("Exception at getClaims() : ", ex);
            throw ex;
        }
    }

    /**
     *  Build the Token Request Preview holding API Endpoint, request payload
     *  @param metadataRequest
     *  @return TokenRequestPreview Object
     *  @throws Exception
     */
    public TokenRequestPreview tokenRequestPreview(TokenMetadataRequest metadataRequest) throws Exception {
        try {
            TokenRequestPreview requestPreview = new TokenRequestPreview();

            requestPreview.payload.grantType = metadataRequest.grantType;

            switch (metadataRequest.grantType)
            {
                case authorization_code:
                    requestPreview.apiEndPoint = this.getClient(metadataRequest.clientId, metadataRequest.clientSec)
                        .buildAPIEndpoint("Token", this.getAppId());
                    requestPreview.payload.clientId = this.getClientId(metadataRequest.clientId);
                    requestPreview.payload.clientSec = this.getClientSecret(metadataRequest.clientSec);
                    requestPreview.payload.redirectUrl = this.getAuthorizationRedirectURL();
                    requestPreview.payload.authorizationCode = metadataRequest.authorizationCode;
                    requestPreview.payload.codeVerifier = metadataRequest.codeVerifier;
                    break;
                case client_credentials:
                    requestPreview.apiEndPoint = this.getClient(settingsService.getOauthServiceUserName(), settingsService.getOauthServiceUserPass())
                        .buildAPIEndpoint("Token", this.getAppId());
                    requestPreview.payload.clientId = settingsService.getOauthServiceUserName();
                    requestPreview.payload.clientSec = settingsService.getOauthServiceUserPass();
                    requestPreview.payload.scope = this.getScopesSupported();
                    break;
                case password:
                    requestPreview.apiEndPoint = this.getClient(settingsService.getOauthServiceUserName(), settingsService.getOauthServiceUserPass())
                        .buildAPIEndpoint("Token", this.getAppId());
                    requestPreview.payload.clientId = settingsService.getOauthServiceUserName();
                    requestPreview.payload.clientSec = settingsService.getOauthServiceUserPass();
                    requestPreview.payload.userName = metadataRequest.userName;
                    requestPreview.payload.password = metadataRequest.password.clone();
                    requestPreview.payload.scope = this.getScopesSupported();
                    Arrays.fill(metadataRequest.password, ' ');
                    break;
                default:
                    throw new Exception("Invalid Grant type is sent");
            }
            return requestPreview;
        }
        catch (Exception ex) {
            logger.error("Exception at tokenRequestPreview() : ", ex);
            throw ex;
        }
    }
}
