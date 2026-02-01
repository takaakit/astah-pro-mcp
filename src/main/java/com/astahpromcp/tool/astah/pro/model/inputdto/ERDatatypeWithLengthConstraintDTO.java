package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDatatypeWithLengthConstraintDTO(
    @JsonPropertyDescription("Target ER datatype identifier")
    String targetERDatatypeId,

    @JsonPropertyDescription("Length constraint: \"None\"/\"Required\"/\"Optional\"")
    String lengthConstraint
) {
}
