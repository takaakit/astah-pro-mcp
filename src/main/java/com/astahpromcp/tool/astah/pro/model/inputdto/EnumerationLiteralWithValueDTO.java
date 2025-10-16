package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EnumerationLiteralWithValueDTO(
    @JsonPropertyDescription("Target enumeration literal identifier")
    String targetEnumerationLiteralId,

    @JsonPropertyDescription("Enumeration literal value")
    String value
) {
}
