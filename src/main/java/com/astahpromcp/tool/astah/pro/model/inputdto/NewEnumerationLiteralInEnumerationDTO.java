package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewEnumerationLiteralInEnumerationDTO(
    @JsonPropertyDescription("Parent enumeration identifier")
    String parentEnumerationId,

    @JsonPropertyDescription("New enumeration literal name")
    String newEnumerationLiteralName
) {
}

