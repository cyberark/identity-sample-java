package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRequestPreview {

    @JsonProperty("apiEndPoint")
    public String apiEndPoint;

    @JsonProperty("payload")
    public TokenMetadataRequest payload = new TokenMetadataRequest();
}
