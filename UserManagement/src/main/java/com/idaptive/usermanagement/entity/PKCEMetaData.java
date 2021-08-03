package com.idaptive.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PKCEMetaData {

    @JsonProperty("codeVerifier")
    public String codeVerifier;

    @JsonProperty("codeChallenge")
    public String codeChallenge;

}