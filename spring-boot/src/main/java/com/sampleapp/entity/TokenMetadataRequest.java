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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenMetadataRequest extends PKCEMetaData {

    @JsonProperty("authFlow")
    public AuthorizationFlow authFlow;

    @JsonProperty("authorizationCode")
    public String authorizationCode;

    @JsonProperty("grantType")
    public GrantType grantType;

    @JsonProperty("userName")
    public String userName;

    @JsonProperty("password")
    public char[] password;

    @JsonProperty("clientId")
    public String clientId;

    @JsonProperty("clientSecret")
    public char[] clientSec;

    @JsonProperty("redirectUrl")
    public String redirectUrl;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("refreshToken")
    public String refreshToken;
}