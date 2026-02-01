package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDomainWithNotNullDTO(
    @JsonPropertyDescription("Target ER domain identifier")
    String targetERDomainId,

    @JsonPropertyDescription("Is NOT NULL. true to set Not Null constraint, false to set Not Null constraint.")
    boolean isNotNull
) {
}
