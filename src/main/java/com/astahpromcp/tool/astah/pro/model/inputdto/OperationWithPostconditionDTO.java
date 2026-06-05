package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record OperationWithPostconditionDTO(
    @JsonPropertyDescription("Target operation identifier")
    String targetOperationId,

    @JsonPropertyDescription("Postcondition. To write multiple lines, use actual newline characters (Unicode U+000A, embedded directly in the string) instead of escape sequences such as \\n.")
    String postcondition
) {
}
