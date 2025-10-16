package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithAlias1DTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Alias1")
    String alias1
) {
}
