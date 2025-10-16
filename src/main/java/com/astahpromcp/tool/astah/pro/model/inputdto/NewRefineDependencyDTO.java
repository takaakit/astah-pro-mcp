package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRefineDependencyDTO(
    @JsonPropertyDescription("Source requirement identifier")
    String sourceRequirementId,
    
    @JsonPropertyDescription("Target requirement identifier")
    String targetRequirementId,
    
    @JsonPropertyDescription("New refine dependency name")
    String newRefineDependencyName
) {
}
