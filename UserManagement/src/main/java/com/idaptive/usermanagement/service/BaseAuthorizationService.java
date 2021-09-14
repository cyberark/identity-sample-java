package com.idaptive.usermanagement.service;

import com.cyberark.client.AuthorizeUrlBuilder;
import com.cyberark.client.CyberArkIdentityOAuthClient;
import com.cyberark.entities.TokenHolder;
import com.cyberark.entities.UserInfo;
import com.cyberark.exception.CyberArkIdentityException;
import com.cyberark.requestBuilders.TokenRequest;
import com.cyberark.utils.PKCEUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.idaptive.usermanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@RefreshScope
@Service
public abstract class BaseAuthorizationService<T extends CyberArkIdentityOAuthClient> {

    @Value("${tenant}")
    protected String tenantURL;

    @Value("${authorizationRedirectURL}")
    private String authorizationRedirectURL;

    private final Logger logger = LoggerFactory.getLogger(BaseAuthorizationService.class);

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

    public BaseAuthorizationService() { }

    /**
     *  Builds the /authorize URL using CyberArkIdentityAuth client
     *  @param metadataRequest Request Metadata
     *  @return authorize URL string
     */
    public String buildAuthorizeURL(AuthorizationMetadataRequest metadataRequest) throws IOException {
        try {
            AuthorizeUrlBuilder authorizeUrlBuilder = this.getClient(metadataRequest.clientId, metadataRequest.clientSecret)
                    .authorizeUrl(this.authorizationRedirectURL)
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
                    .requestToken(tokenMetadataRequest.authorizationCode, this.authorizationRedirectURL)
                    .setGrantType(tokenMetadataRequest.grantType.name());

            if (tokenMetadataRequest.codeVerifier != null){
                tokenRequest.setCodeVerifier(tokenMetadataRequest.codeVerifier);
            }
            tokenHolder = tokenRequest.execute();
            return tokenHolder;
        }
        catch (CyberArkIdentityException ex) {
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
            claims = CyberArkIdentityOAuthClient.claims(token);
            return claims;
        }
        catch (CyberArkIdentityException ex) {
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
            requestPreview.apiEndPoint = this.getClient(metadataRequest.clientId, metadataRequest.clientSec)
                    .buildAPIEndpoint("Token", this.getAppId());

            requestPreview.payload.clientId = this.getClientId(metadataRequest.clientId);
            requestPreview.payload.clientSec = this.getClientSecret(metadataRequest.clientSec);
            requestPreview.payload.grantType = metadataRequest.grantType;

            switch (metadataRequest.grantType)
            {
                case authorization_code:
                    requestPreview.payload.redirectUrl = this.authorizationRedirectURL;
                    requestPreview.payload.authorizationCode = metadataRequest.authorizationCode;
                    requestPreview.payload.codeVerifier = metadataRequest.codeVerifier;
                    break;
                case client_credentials:
                    requestPreview.payload.scope = this.getScopesSupported();
                    break;
                case password:
                    requestPreview.payload.userName = metadataRequest.userName;
                    requestPreview.payload.password = metadataRequest.password;
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
