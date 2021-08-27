package com.idaptive.usermanagement.entity;

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
    public String password;

    @JsonProperty("clientId")
    public String clientId;

    @JsonProperty("clientSecret")
    public String clientSecret;

    @JsonProperty("redirectUrl")
    public String redirectUrl;

    @JsonProperty("scope")
    public String scope;
}