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
