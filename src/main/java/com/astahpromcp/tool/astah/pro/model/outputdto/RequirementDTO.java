package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RequirementDTO(
    @JsonPropertyDescription("Class information")
    ClassDTO clazz,

    @JsonPropertyDescription("Requirement identifier (The unique id of the requirement)")
    String requirementId,

    @JsonPropertyDescription("Requirement text")
    String requirementText
) {
}
