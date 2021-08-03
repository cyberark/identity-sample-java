package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenMetadataRequest extends PKCEMetaData {

    @JsonProperty("authorizationCode")
    public String authorizationCode;

}
