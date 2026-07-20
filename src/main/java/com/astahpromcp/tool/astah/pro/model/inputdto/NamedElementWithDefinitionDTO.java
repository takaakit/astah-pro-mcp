package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithDefinitionDTO(
    @JsonPropertyDescription("Target named element identifier")
    String targetNamedElementId,
    
    @JsonPropertyDescription("Definition (In other words, a descriptive text). The definition is automatically wrapped at the right edge, so there is no need to insert line breaks in the middle of a sentence to fit the definition's display width.")
    String definition
) {
}
