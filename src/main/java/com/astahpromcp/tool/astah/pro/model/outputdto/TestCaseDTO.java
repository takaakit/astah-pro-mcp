package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record TestCaseDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO clazz,

    @JsonPropertyDescription("Test case identifier")
    String testCaseId
) {
}
