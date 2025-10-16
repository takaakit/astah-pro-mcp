package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTestCaseInPackageDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,
    
    @JsonPropertyDescription("New test case name")
    String newTestCaseName
) {
}
