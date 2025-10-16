package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ElementWithStereotypeDTO(
    @JsonPropertyDescription("Target element identifier")
    String id,

    @JsonPropertyDescription("Stereotype")
    String stereotype
) {
}
