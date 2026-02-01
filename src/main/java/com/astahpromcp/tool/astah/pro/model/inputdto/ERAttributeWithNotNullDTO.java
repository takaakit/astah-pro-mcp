package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithNotNullDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("Is NOT NULL. true to set Not Null constraint, false to set Not Null constraint.")
    boolean isNotNull
) {
}
