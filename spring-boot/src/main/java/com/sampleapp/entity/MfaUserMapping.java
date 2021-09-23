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
