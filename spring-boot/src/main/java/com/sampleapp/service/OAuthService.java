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

import com.cyberark.client.OAuthClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.cyberark.exception.IdentityException;
import com.sampleapp.entity.OIDCTokens;
import com.sampleapp.entity.TokenMetadataRequest;
import com.sampleapp.entity.AuthorizationFlow;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OAuthService extends BaseAuthorizationService<OAuthClient>{

    private final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    @Autowired
    private SettingsService settingsService;

    public OAuthService() { }

    @Override
    public AuthorizationFlow supportedAuthorizationFlow() { return AuthorizationFlow.OAUTH; }

    @Override
    protected String getAppId() { return settingsService.getOauthApplicationID(); }

    @Override
    protected String getClientId(String clientId) { return clientId; }

    @Override
    protected char[] getClientSecret(char[] clientSecret) { return clientSecret; }

    @Override
    protected String getScopesSupported() { return settingsService.getOauthScopesSupported(); }


    /**
     *  Get CyberArkIdentityOAuthClient Instance to make authorized API requests.
     *  @param clientId         OAuth App Client Id
     *  @param clientSec     OAuth App Client Secret
     *  @return CyberArkIdentityOAuthClient Instance to make authorized API requests.
     *  @throws IOException
     */
    @Override
    public OAuthClient getClient(String clientId, char[] clientSec) throws IOException {
        if (clientSec == null){
            return new OAuthClient(settingsService.getTenantURL(), settingsService.getOauthApplicationID(), clientId);
        }
        else {
            return new OAuthClient(settingsService.getTenantURL(), settingsService.getOauthApplicationID(), clientId, String.valueOf(clientSec));
        }
    }

    /**
     *  Get Tokens using Client Credentials Grant flow with reference to CyberArkIdentityOAuthClient.
     *  @param tokenMetadataRequest Input parameter
     *  @return TokenHolder An Object that holds access_token, refresh_token, token_type, scope, expires_in
     */
    @Override
    public TokenHolder getTokenSetWithClientCreds(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        TokenHolder tokenHolder;
        try {
            tokenHolder = (TokenHolder) this.getClient(settingsService.getOauthServiceUserName(), settingsService.getOauthServiceUserPass())
                    .requestTokenWithClientCreds()
                    .setGrantType(tokenMetadataRequest.grantType.name())
                    .setScope(this.getScopesSupported())
                    .execute();
            return tokenHolder;
        } catch (IdentityException ex) {
            logger.error("Exception at getTokenSetWithClientCreds() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get Tokens using Resource Owner Password Grant flow with reference to CyberArkIdentityOAuthClient.
     *  @param tokenMetadataRequest Input parameter
     *  @return TokenHolder An Object that holds access_token, refresh_token, token_type, scope, expires_in
     */
    @Override
    public TokenHolder getTokenSetWithPassword(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        TokenHolder tokenHolder;
        try {
            tokenHolder = (TokenHolder) this.getClient(settingsService.getOauthServiceUserName(), settingsService.getOauthServiceUserPass())
                    .requestTokenWithPassword(tokenMetadataRequest.userName, String.valueOf(tokenMetadataRequest.password))
                    .setGrantType(tokenMetadataRequest.grantType.name())
                    .setScope(this.getScopesSupported())
                    .execute();
            return tokenHolder;
        } catch (IdentityException ex) {
            logger.error("Exception at getTokenSetWithPassword() : ", ex);
            throw ex;
        }
    }

    @Override
    public Boolean revokeToken(OIDCTokens oidcTokens) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public UserInfo getUserInfo(String accessToken) throws IOException {
        throw new NotImplementedException();
    }
}
