package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithAlias2DTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Alias2")
    String alias2
) {
}
