package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageRegion {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("full")
    FULL,
    @JsonProperty("top_left")
    TOP_LEFT,
    @JsonProperty("top_right")
    TOP_RIGHT,
    @JsonProperty("bottom_left")
    BOTTOM_LEFT,
    @JsonProperty("bottom_right")
    BOTTOM_RIGHT;
}
