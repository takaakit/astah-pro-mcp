package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RequirementWithIdDTO(
    @JsonPropertyDescription("Target requirement identifier")
    String id,
    
    @JsonPropertyDescription("Requirement identifier (The unique id of the requirement)")
    String requirementId
) {
}

