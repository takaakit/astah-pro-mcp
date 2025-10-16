package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RequirementWithTextDTO(
    @JsonPropertyDescription("Target requirement identifier")
    String id,
    
    @JsonPropertyDescription("Requirement text")
    String requirementText
) {
}

