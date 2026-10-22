package com.astahpromcp.tool.projectdiff.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ElementRefDTO(
    @JsonPropertyDescription("xmi.id; empty if none")
    String xmiId,

    @JsonPropertyDescription("Name; empty if none")
    String name
) {
    public static ElementRefDTO empty() {
        return new ElementRefDTO(
            "",
            "");
    }
}
