package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRefineDependencyDTO(
    @JsonPropertyDescription("Source named element identifier")
    String sourceNamedElementId,
    
    @JsonPropertyDescription("Target requirement identifier")
    String targetRequirementId
) {
}
