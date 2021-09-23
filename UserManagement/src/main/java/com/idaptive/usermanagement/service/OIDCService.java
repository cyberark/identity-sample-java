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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OIDCService extends BaseAuthorizationService<CyberArkIdentityOIDCClient> {

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
     *  Get CyberArkIdentityOIDCClient Instance to make authorized API requests.
     *  @param clientId         OIDC App Client Id
     *  @param clientSecret     OIDC App Client Secret
     *  @return CyberArkIdentityOIDCClient Instance to make authorized API requests.
     *  @throws IOException
     */
    @Override
    public CyberArkIdentityOIDCClient getClient(String clientId, char[] clientSecret) throws IOException {
        return new CyberArkIdentityOIDCClient(settingsService.getTenantURL(), settingsService.getOIDCApplicationID(), settingsService.getOIDCClientID());
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
