package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum VisibilityKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("public")
    PUBLIC("public"),
    @JsonProperty("protected")
    PROTECTED("protected"),
    @JsonProperty("private")
    PRIVATE("private"),
    @JsonProperty("package")
    PACKAGE("package");

    public final String astahValue;

    private VisibilityKind(String astahValue) {
        this.astahValue = astahValue;
    }
}
