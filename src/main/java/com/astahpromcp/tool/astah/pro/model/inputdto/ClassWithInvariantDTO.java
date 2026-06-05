package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassWithInvariantDTO(
    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("Invariant. To write multiple lines, use actual newline characters (Unicode U+000A, embedded directly in the string) instead of escape sequences such as \\n.")
    String invariant
) {
}
