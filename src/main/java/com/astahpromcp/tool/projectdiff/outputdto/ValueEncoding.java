package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonProperty;

// How a shown value relates to the value written in the XMI
public enum ValueEncoding {
    // Some AI agents tend to specify ENUM literals in lowercase even when they are defined in uppercase, so JSON keys should be defined in lowercase using @JsonProperty.
    @JsonProperty("decoded")
    DECODED,
    @JsonProperty("raw")
    RAW,
    @JsonProperty("unknown")
    UNKNOWN
}
