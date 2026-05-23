package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record OperationWithPostconditionDTO(
    @JsonPropertyDescription("Target operation identifier")
    String targetOperationId,

    @JsonPropertyDescription("Postcondition. To write multiple lines, use newline characters (\\n).")
    String postcondition
) {
}
