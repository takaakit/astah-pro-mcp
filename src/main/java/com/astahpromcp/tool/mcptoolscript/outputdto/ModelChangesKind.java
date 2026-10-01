package com.astahpromcp.tool.mcptoolscript.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// What became of the changes an mcp tool script run made to the model.
public enum ModelChangesKind {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("none")
    NONE,
    @JsonProperty("committed")
    COMMITTED,
    @JsonProperty("rolled_back")
    ROLLED_BACK,
    @JsonProperty("unknown")
    UNKNOWN
}
