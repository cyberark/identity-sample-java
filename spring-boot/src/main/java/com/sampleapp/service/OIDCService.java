/*
 * Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.
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

import com.cyberark.client.OIDCClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sampleapp.entity.TokenMetadataRequest;
import com.sampleapp.entity.OIDCTokens;
import com.cyberark.exception.IdentityException;
import com.sampleapp.entity.AuthorizationFlow;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OIDCService extends BaseAuthorizationService<OIDCClient> {

    private final Logger logger = LoggerFactory.getLogger(OIDCService.class);

    @Autowired
    private SettingsService settingsService;

    public OIDCService() { }

    @Override
    public AuthorizationFlow supportedAuthorizationFlow() { return AuthorizationFlow.OIDC; }

    @Override
    protected String getAppId() { return settingsService.getOIDCApplicationID(); }

    @Override
    protected String getClientId(String clientId) { return settingsService.getOIDCClientID(); }

    @Override
    protected char[] getClientSecret(char[] clientSecret) { return null; }

    @Override
    protected String getScopesSupported() {
        return settingsService.getOIDCScopesSupported();
    }


    /**
     *  Get OIDCClient Instance to make authorized API requests.
     *  @param clientId         OIDC App Client Id
     *  @param clientSecret     OIDC App Client Secret
     *  @return OIDCClient Instance to make authorized API requests.
     *  @throws IOException
     */
    @Override
    public OIDCClient getClient(String clientId, char[] clientSecret) throws IOException {
        return new OIDCClient(settingsService.getTenantURL(), settingsService.getOIDCApplicationID(), settingsService.getOIDCClientID());
    }

    /**
     *  Get UserInfo using OIDCClient
     *  @param accessToken Input string
     *  @return UserInfo An Object that holds user related info.
     */
    @Override
    public UserInfo getUserInfo(String accessToken) throws IOException {
        UserInfo userInfo;
        try {
            userInfo = this.getClient(null, null)
                    .userInfo(accessToken)
                    .execute();
            return userInfo;
        } catch (IdentityException ex) {
            logger.error("Exception at getUserInfo() : ", ex);
            throw ex;
        }
    }

    /**
     *  Revoke Access Tokens, ID Token using OIDCClient
     *  Revoking ID Token as the lifetime of id_token is equivalent to Access Token
     *  @param oidcTokens Holds Access Token and ID Token received from Authorize Response(Frontend) and also Access Token received from Token Endpoint (Backend).
     *  @return true on success.
     */
    @Override
    public Boolean revokeToken(OIDCTokens oidcTokens) throws IOException {
        try {
            OIDCClient oidcClient = new OIDCClient(settingsService.getTenantURL(), settingsService.getOIDCApplicationID(), settingsService.getOIDCClientID(), String.valueOf(settingsService.getOIDCClientPass()));

            if (oidcTokens.authResponseAccessToken != null) {
                oidcClient.revokeToken(oidcTokens.authResponseAccessToken)
                        .execute();
            }
            if (oidcTokens.authResponseIDToken != null) {
                oidcClient.revokeToken(oidcTokens.authResponseIDToken)
                        .execute();
            }
            if (oidcTokens.tokenResponseAccessToken != null) {
                oidcClient.revokeToken(oidcTokens.tokenResponseAccessToken)
                        .execute();
            }
            return true;
        }
        catch (IdentityException ex) {
            logger.error("Exception at revokeToken() : ", ex);
            throw ex;
        }
    }

    @Override
    public JsonNode introspect(String accessToken) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public TokenHolder getTokenSetWithClientCreds(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public TokenHolder getTokenSetWithPassword(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        throw new NotImplementedException();
    }
}
