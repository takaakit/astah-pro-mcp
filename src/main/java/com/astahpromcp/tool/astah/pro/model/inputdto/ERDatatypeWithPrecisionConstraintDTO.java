package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDatatypeWithPrecisionConstraintDTO(
    @JsonPropertyDescription("Target ER datatype identifier")
    String targetERDatatypeId,

    @JsonPropertyDescription("Precision constraint: \"None\"/\"Required\"/\"Optional\"")
    String precisionConstraint
) {
}
