package com.idaptive.usermanagement.service;

import com.cyberark.client.CyberArkIdentityOIDCClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.cyberark.exception.CyberArkIdentityException;
import com.cyberark.utils.PKCEUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.idaptive.usermanagement.entity.PKCEMetaData;
import com.idaptive.usermanagement.entity.TokenMetadataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@RefreshScope
@Service
public class OIDCService {

    private final Logger logger = LoggerFactory.getLogger(OIDCService.class);

    @Value("${tenant}")
    private String tenantURL;

    @Value("${oidcAppId}")
    private String oidcAppId;

    @Value("${oidcClientId}")
    private String oidcClientId;

    @Value("${oidcRedirectURL}")
    private String oidcRedirectURL;

    @Value("${oidcScopesSupported}")
    private String scopesSupported;

    private final String responseType = "code";

    private final String codeChallengeMethod = "S256";

    /**
     *  Builds the /authorize URL using CyberArkIdentityAuth client
     *  @param codeChallenge Input string
     *  @return authorize URL string
     */
    public String buildAuthorizeURL(String codeChallenge) throws IOException {
        String authorizeUrl;
        try
        {
            CyberArkIdentityOIDCClient identityAuth = new CyberArkIdentityOIDCClient(tenantURL, oidcAppId, oidcClientId);

            authorizeUrl = identityAuth.authorizeUrl(oidcRedirectURL)
                    .setResponseType(responseType)
                    .setScope(scopesSupported)
                    .setCodeChallenge(codeChallenge)
                    .setCodeChallengeMethod(codeChallengeMethod)
                    .build();

            return authorizeUrl;
        }
        catch (Exception ex){
            logger.error("Exception at buildAuthorizeURL() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get PKCEMetaData which holds PKCEMetaData and CodeChallenge using CyberArkIdentityAuth client
     *  @return PKCEMetaData An Object that holds PKCEMetaData and CodeChallenge
     */
    public PKCEMetaData getPKCEMetaData() throws Exception {
        try
        {
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
     *  Get Tokens using CyberArkIdentityAuth client
     *  @param tokenMetadataRequest Input parameter
     *  @return TokenHolder An Object that holds access_token, id_token, refresh_token, token_type, scope, expires_in
     */
    public TokenHolder getTokenSet(TokenMetadataRequest tokenMetadataRequest) throws IOException {
        TokenHolder tokenHolder;
        try {
            CyberArkIdentityOIDCClient identityAuth = new CyberArkIdentityOIDCClient(tenantURL, oidcAppId, oidcClientId);

            tokenHolder = identityAuth.requestToken(tokenMetadataRequest.authorizationCode, oidcRedirectURL)
                    .setGrantType("authorization_code")
                    .setCodeVerifier(tokenMetadataRequest.codeVerifier)
                    .execute();

            return tokenHolder;
        } catch (CyberArkIdentityException ex) {
            logger.error("Exception at getTokenSet() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get Claims using CyberArkIdentityAuth client
     *  @param idToken Input string
     *  @return JsonNode An Object that holds base64 decoded id_token as claims
     */
    public JsonNode getClaims(String idToken) throws IOException {
        JsonNode claims;
        try {
            CyberArkIdentityOIDCClient identityAuth = new CyberArkIdentityOIDCClient(tenantURL, oidcAppId, oidcClientId);
            claims = identityAuth.claims(idToken);
            return claims;
        } catch (CyberArkIdentityException ex) {
            logger.error("Exception at getClaims() : ", ex);
            throw ex;
        }
    }

    /**
     *  Get UserInfo using CyberArkIdentityAuth client
     *  @param accessToken Input string
     *  @return UserInfo An Object that holds user related info.
     */
    public UserInfo getUserInfo(String accessToken) throws IOException {
        UserInfo userInfo;
        try {
            CyberArkIdentityOIDCClient identityAuth = new CyberArkIdentityOIDCClient(tenantURL, oidcAppId, oidcClientId);
            userInfo = identityAuth.userInfo(accessToken).execute();
            return userInfo;
        } catch (CyberArkIdentityException ex) {
            logger.error("Exception at getUserInfo() : ", ex);
            throw ex;
        }
    }
}
