package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CombinedFragmentKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("alt")
    ALT("alt"),
    @JsonProperty("assert")
    ASSERT("assert"),
    @JsonProperty("break")
    BREAK("break"),
    @JsonProperty("consider")
    CONSIDER("consider"),
    @JsonProperty("critical")
    CRITICAL("critical"),
    @JsonProperty("ignore")
    IGNORE("ignore"),
    @JsonProperty("loop")
    LOOP("loop"),
    @JsonProperty("neg")
    NEG("neg"),
    @JsonProperty("opt")
    OPT("opt"),
    @JsonProperty("par")
    PAR("par"),
    @JsonProperty("seq")
    SEQ("seq"),
    @JsonProperty("strict")
    STRICT("strict");

    public final String astahValue;

    private CombinedFragmentKind(String astahValue) {
        this.astahValue = astahValue;
    }
}
