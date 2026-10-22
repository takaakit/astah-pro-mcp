package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Where a comparison stands
public enum DiffStatus {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("running")
    RUNNING,
    @JsonProperty("done")
    DONE,
    @JsonProperty("failed")
    FAILED
}
