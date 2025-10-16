package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkEndWithNavigationDTO(
    @JsonPropertyDescription("Target link end identifier")
    String targetLinkEndId,
    
    @JsonPropertyDescription("Is Navigation")
    boolean isNavigation
) {
}
