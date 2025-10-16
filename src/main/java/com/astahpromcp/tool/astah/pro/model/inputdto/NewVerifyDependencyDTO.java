package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewVerifyDependencyDTO(
    @JsonPropertyDescription("Source test case identifier")
    String sourceTestCaseId,
    
    @JsonPropertyDescription("Target requirement identifier")
    String targetRequirementId,
    
    @JsonPropertyDescription("New verify dependency name")
    String newVerifyDependencyName
) {
}
