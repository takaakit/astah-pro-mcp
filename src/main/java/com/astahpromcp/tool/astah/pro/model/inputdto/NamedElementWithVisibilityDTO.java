package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.VisibilityKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithVisibilityDTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Visibility")
    VisibilityKind visibility
) {
}
