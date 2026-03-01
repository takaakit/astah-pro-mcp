package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NavigabilityKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("navigable")
    NAVIGABLE("Navigable"),
    @JsonProperty("non_navigable")
    NON_NAVIGABLE("Non_Navigable"),
    @JsonProperty("unspecified")
    UNSPECIFIED("Unspecified");

    public final String astahValue;

    private NavigabilityKind(String astahValue) {
        this.astahValue = astahValue;
    }
}