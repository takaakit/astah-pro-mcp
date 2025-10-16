package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TransitionWithActionDTO(
    @JsonPropertyDescription("Target transition identifier")
    String targetTransitionId,
    
    @JsonPropertyDescription("Action")
    String action
) {
}
