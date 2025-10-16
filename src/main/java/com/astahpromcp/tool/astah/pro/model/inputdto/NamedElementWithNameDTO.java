package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithNameDTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Name")
    String name
) {
}
