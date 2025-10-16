package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TransitionWithEventDTO(
    @JsonPropertyDescription("Target transition identifier")
    String targetTransitionId,
    
    @JsonPropertyDescription("Event")
    String event
) {
}
