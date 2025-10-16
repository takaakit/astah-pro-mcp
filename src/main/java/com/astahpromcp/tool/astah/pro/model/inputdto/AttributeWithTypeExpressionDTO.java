package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithTypeExpressionDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,

    @JsonPropertyDescription("Type expression: The string representation of a type, consisting of the type name and its modifiers.")
    String typeExpression
) {
}
