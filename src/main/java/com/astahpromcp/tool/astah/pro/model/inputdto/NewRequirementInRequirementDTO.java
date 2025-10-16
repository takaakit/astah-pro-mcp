package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRequirementInRequirementDTO(
    @JsonPropertyDescription("Parent requirement identifier")
    String parentRequirementId,
    
    @JsonPropertyDescription("New requirement name")
    String newRequirementName
) {
}
