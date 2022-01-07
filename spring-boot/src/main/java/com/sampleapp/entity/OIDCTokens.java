package com.sampleapp.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCTokens {
    @JsonProperty("authResponseIDToken")
    public String authResponseIDToken;

    @JsonProperty("authResponseAccessToken")
    public String authResponseAccessToken;

    @JsonProperty("tokenResponseAccessToken")
    public String tokenResponseAccessToken;
}
