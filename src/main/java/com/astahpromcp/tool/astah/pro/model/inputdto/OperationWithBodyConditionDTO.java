package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record OperationWithBodyConditionDTO(
    @JsonPropertyDescription("Target operation identifier")
    String targetOperationId,

    @JsonPropertyDescription("Body condition. To write multiple lines, use actual newline characters (Unicode U+000A, embedded directly in the string) instead of escape sequences such as \\n.")
    String bodyCondition
) {
}
