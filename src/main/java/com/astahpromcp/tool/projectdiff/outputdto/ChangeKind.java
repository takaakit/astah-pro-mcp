package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// What happened to an element, or to one value of an element
public enum ChangeKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("added")
    ADDED,
    @JsonProperty("removed")
    REMOVED,
    @JsonProperty("modified")
    MODIFIED
}
