package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithInitialValueDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,

    @JsonPropertyDescription("Initial value")
    String initialValue
) {
}
