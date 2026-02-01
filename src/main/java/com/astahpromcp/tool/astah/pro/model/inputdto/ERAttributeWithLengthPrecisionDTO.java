package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithLengthPrecisionDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("Length/Precision. Length/Precision should be number. ex) value \"10\" : Length 10, value \"10,5\" : Length 10, Precision 5")
    String lengthPrecision
) {
}
