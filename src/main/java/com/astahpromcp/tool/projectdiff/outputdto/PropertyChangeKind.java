package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// What happened to one value of a modified element. Kept apart from ChangeKind, which says what happened to an element.
public enum PropertyChangeKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("added")
    ADDED,

    @JsonProperty("removed")
    REMOVED,

    @JsonProperty("modified")
    MODIFIED,

    // The same references in another order
    @JsonProperty("reordered")
    REORDERED
}
