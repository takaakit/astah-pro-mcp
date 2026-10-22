package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Where one side of a comparison came from
public enum ProjectKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("file")
    FILE,
    @JsonProperty("open_project")
    OPEN_PROJECT
}
