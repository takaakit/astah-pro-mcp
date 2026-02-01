package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDatatypeWithDefaultLengthPrecisionDTO(
    @JsonPropertyDescription("Target ER datatype identifier")
    String targetERDatatypeId,

    @JsonPropertyDescription("Default length/precision. Default length/precision should be number. ex) value \"10\" : Length 10, value \"10,5\" : Length 10, Precision 5")
    String defaultLengthPrecision
) {
}
