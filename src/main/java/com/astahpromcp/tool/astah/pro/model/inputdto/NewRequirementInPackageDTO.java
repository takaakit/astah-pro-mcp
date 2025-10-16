package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRequirementInPackageDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,
    
    @JsonPropertyDescription("New requirement name")
    String newRequirementName
) {
}
