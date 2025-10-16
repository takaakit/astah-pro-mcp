package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTraceDependencyDTO(
    @JsonPropertyDescription("Source requirement identifier")
    String sourceRequirementId,
    
    @JsonPropertyDescription("Target requirement identifier")
    String targetRequirementId,
    
    @JsonPropertyDescription("New trace dependency name")
    String newTraceDependencyName
) {
}
