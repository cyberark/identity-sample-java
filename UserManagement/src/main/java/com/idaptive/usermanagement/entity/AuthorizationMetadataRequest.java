package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationMetadataRequest extends PKCEMetaData {

    @JsonProperty("authFlow")
    public AuthorizationFlow authFlow;

    @JsonProperty("clientId")
    public String clientId;

    @JsonProperty("clientSecret")
    public char[] clientSecret;

    @JsonProperty("responseType")
    public String responseType;
}
