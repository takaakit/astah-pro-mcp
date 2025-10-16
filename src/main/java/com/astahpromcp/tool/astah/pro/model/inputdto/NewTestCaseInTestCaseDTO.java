package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTestCaseInTestCaseDTO(
    @JsonPropertyDescription("Parent test case identifier")
    String parentTestCaseId,
    
    @JsonPropertyDescription("New test case name")
    String newTestCaseName
) {
}
