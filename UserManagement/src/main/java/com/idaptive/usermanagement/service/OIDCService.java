package com.idaptive.usermanagement.service;

import com.cyberark.client.CyberArkIdentityOIDCClient;
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

@RefreshScope
@Service
public class OIDCService extends BaseAuthorizationService<CyberArkIdentityOIDCClient> {

    private final Logger logger = LoggerFactory.getLogger(OIDCService.class);

    @Value("${oidcAppId}")
    private String oidcAppId;

    @Value("${oidcClientId}")
    private String oidcClientId;

    @Value("${oidcScopesSupported}")
    private String oidcScopesSupported;

    public OIDCService() { }

    @Override
    public AuthorizationFlow supportedAuthorizationFlow() { return AuthorizationFlow.OIDC; }

    @Override
    protected String getAppId() { return this.oidcAppId; }

    @Override
    protected String getClientId(String clientId) { return this.oidcClientId; }

    @Override
    protected String getClientSecret(String clientSecret) { return null; }

    @Override
    protected String getScopesSupported() {
        return this.oidcScopesSupported;
    }


    /**
     *  Get CyberArkIdentityOIDCClient Instance to make authorized API requests.
     *  @param clientId         OIDC App Client Id
     *  @param clientSecret     OIDC App Client Secret
     *  @return CyberArkIdentityOIDCClient Instance to make authorized API requests.
     *  @throws IOException
     */
    @Override
    public CyberArkIdentityOIDCClient getClient(String clientId, String clientSecret) throws IOException {
        return new CyberArkIdentityOIDCClient(super.tenantURL, this.oidcAppId, this.oidcClientId);
    }

    /**
     *  Get UserInfo using CyberArkIdentityOIDCClient
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
        } catch (CyberArkIdentityException ex) {
            logger.error("Exception at getUserInfo() : ", ex);
            throw ex;
        }
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
