package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// The stages a comparison goes through
public enum DiffStage {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("none")
    NONE,
    @JsonProperty("file_export")
    FILE_EXPORT,
    @JsonProperty("compare")
    COMPARE,
    @JsonProperty("result")
    RESULT
}
