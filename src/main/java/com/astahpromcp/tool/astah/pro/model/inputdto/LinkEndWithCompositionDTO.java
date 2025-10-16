package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkEndWithCompositionDTO(
    @JsonPropertyDescription("Target link end identifier")
    String targetLinkEndId,
    
    @JsonPropertyDescription("Is Composition")
    boolean isComposition
) {
}
