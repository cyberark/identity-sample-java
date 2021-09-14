package com.idaptive.usermanagement.service;

import com.cyberark.client.CyberArkIdentityOAuthClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.cyberark.exception.CyberArkIdentityException;
import com.idaptive.usermanagement.entity.AuthorizationFlow;
import com.idaptive.usermanagement.entity.TokenMetadataRequest;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@RefreshScope
@Service
public class OAuthService extends BaseAuthorizationService<CyberArkIdentityOAuthClient>{

    private final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    @Value("${oauthAppId}")
    private String oauthAppId;

    @Value("${oauthServiceUserName}")
    private String oauthServiceUserName;

    @Value("${oauthServiceUserPassword}")
    private char[] oauthServiceUserPass;

    @Value("${oauthScopesSupported}")
    private String oauthScopesSupported;

    public OAuthService() { }

    @Override
    public AuthorizationFlow supportedAuthorizationFlow() { return AuthorizationFlow.OAUTH; }

    @Override
    protected String getAppId() { return this.oauthAppId; }

    @Override
    protected String getClientId(String clientId) { return clientId; }

    @Override
    protected char[] getClientSecret(char[] clientSecret) { return clientSecret; }

    @Override
    protected String getScopesSupported() { return this.oauthScopesSupported; }


    /**
     *  Get CyberArkIdentityOAuthClient Instance to make authorized API requests.
     *  @param clientId         OAuth App Client Id
     *  @param clientSec     OAuth App Client Secret
     *  @return CyberArkIdentityOAuthClient Instance to make authorized API requests.
     *  @throws IOException
     */
    @Override
    public CyberArkIdentityOAuthClient getClient(String clientId, char[] clientSec) throws IOException {
        if (clientSec == null){
            return new CyberArkIdentityOAuthClient(super.tenantURL, this.oauthAppId, clientId);
        }
        else {
            return new CyberArkIdentityOAuthClient(super.tenantURL, this.oauthAppId, clientId, String.valueOf(clientSec));
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
            tokenHolder = this.getClient(this.oauthServiceUserName, this.oauthServiceUserPass)
                    .requestTokenWithClientCreds()
                    .setGrantType(tokenMetadataRequest.grantType.name())
                    .setScope(this.getScopesSupported())
                    .execute();
            return tokenHolder;
        } catch (CyberArkIdentityException ex) {
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
            tokenHolder = this.getClient(this.oauthServiceUserName, this.oauthServiceUserPass)
                    .requestTokenWithPassword(tokenMetadataRequest.userName, String.valueOf(tokenMetadataRequest.password))
                    .setGrantType(tokenMetadataRequest.grantType.name())
                    .setScope(this.getScopesSupported())
                    .execute();
            return tokenHolder;
        } catch (CyberArkIdentityException ex) {
            logger.error("Exception at getTokenSetWithPassword() : ", ex);
            throw ex;
        }
    }

    @Override
    public UserInfo getUserInfo(String accessToken) throws IOException {
        throw new NotImplementedException();
    }
}
