package com.idaptive.usermanagement.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TokenStore {

    private Integer UserId;
    private String SessionUuid;
    private String MfaToken;

    public TokenStore(){
    }
    public TokenStore(Integer userId,String sessionUuid,String mfaToken){
    this.UserId = userId;
    this.SessionUuid = sessionUuid;
    this.MfaToken = mfaToken;
    }
    @Id
    public Integer getUserId() {
        return UserId;
    }

    public void setUserId(Integer userId) {
        this.UserId = userId;
    }

    public String getSessionUuid() {
        return SessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        this.SessionUuid = sessionUuid;
    }

    @Column(length = 1000)
    public String getMfaToken() {
        return MfaToken;
    }

    public void setMfaToken(String mfaToken) {
        MfaToken = mfaToken;
    }

}
