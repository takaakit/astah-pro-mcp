package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassWithInvariantDTO(
    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("Invariant. To write multiple lines, use newline characters (\\n).")
    String invariant
) {
}
