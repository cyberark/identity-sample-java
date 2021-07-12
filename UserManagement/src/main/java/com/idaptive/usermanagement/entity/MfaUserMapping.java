package com.idaptive.usermanagement.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MfaUserMapping {
    private  Integer UserId;
    private  String MfaUserId;

    public MfaUserMapping(Integer userId,String mfaUserId){
        this.UserId = userId;
        this.MfaUserId = mfaUserId;
    }
    public MfaUserMapping(){

    }
    @Id
    public Integer getUserId() {
        return this.UserId;
    }

    public void setUserId(Integer userId) {
        this.UserId = userId;
    }

    public String getMfaUserId() {
        return this.MfaUserId;
    }

    public void setMfaUserId(String mfaUserId) {
        this.MfaUserId = mfaUserId;
    }
}
