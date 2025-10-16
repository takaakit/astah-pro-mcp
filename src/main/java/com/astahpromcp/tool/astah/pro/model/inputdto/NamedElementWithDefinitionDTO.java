package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithDefinitionDTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Definition (In other words, a descriptive text)")
    String definition
) {
}
