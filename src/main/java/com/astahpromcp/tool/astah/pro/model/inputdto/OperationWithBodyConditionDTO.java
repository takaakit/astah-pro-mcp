package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record OperationWithBodyConditionDTO(
    @JsonPropertyDescription("Target operation identifier")
    String targetOperationId,

    @JsonPropertyDescription("Body condition. To write multiple lines, use newline characters (\\n).")
    String bodyCondition
) {
}
