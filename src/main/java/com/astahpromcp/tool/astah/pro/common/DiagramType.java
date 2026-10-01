package com.astahpromcp.tool.astah.pro.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DiagramType {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("class")
    CLASS,
    @JsonProperty("sequence")
    SEQUENCE,
    @JsonProperty("activity")
    ACTIVITY,
    @JsonProperty("state_machine")
    STATE_MACHINE,
    @JsonProperty("use_case")
    USE_CASE,
    @JsonProperty("communication")
    COMMUNICATION,
    @JsonProperty("composite_structure")
    COMPOSITE_STRUCTURE,
    @JsonProperty("requirement")
    REQUIREMENT,
    @JsonProperty("er")
    ER,
    @JsonProperty("mind_map")
    MIND_MAP
}
