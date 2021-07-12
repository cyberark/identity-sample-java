package com.idaptive.usermanagement.entity;

public class AdvanceLoginRequest {
    private String SessionUuid;
    private String AuthorizationCode;

    public AdvanceLoginRequest(){

    }
    public String getSessionUuid() {
        return SessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        SessionUuid = sessionUuid;
    }

    public String getAuthorizationCode() {
        return AuthorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.AuthorizationCode = authorizationCode;
    }
}
