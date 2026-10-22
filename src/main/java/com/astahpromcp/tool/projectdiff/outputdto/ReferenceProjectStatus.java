package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Whether a referenced project is up to date
public enum ReferenceProjectStatus {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("latest")
    LATEST,
    @JsonProperty("need_update")
    NEED_UPDATE,
    @JsonProperty("not_found")
    NOT_FOUND,
    @JsonProperty("unknown")
    UNKNOWN
}
