package com.idaptive.usermanagement.entity;

public class AdvanceLoginRequest {
    private String SessionUuid;
    private String AuthorizationCode;
    private String ClientId;
    private String CodeVerifier;

    public AdvanceLoginRequest(){

    }
    public String getSessionUuid() {
        return this.SessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        this.SessionUuid = sessionUuid;
    }

    public String getAuthorizationCode() {
        return this.AuthorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.AuthorizationCode = authorizationCode;
    }

    public String getClientId() {
        return this.ClientId;
    }

    public void setClientId(String clientId) {
        this.ClientId = clientId;
    }

    public String getCodeVerifier() {
        return this.CodeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.CodeVerifier = codeVerifier;
    }
}
