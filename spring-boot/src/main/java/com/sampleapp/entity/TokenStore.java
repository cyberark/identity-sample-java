/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
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

package com.sampleapp.entity;

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
