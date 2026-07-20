package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RequirementDTO(
    @JsonPropertyDescription("Class info")
    @JsonProperty("class")
    ClassDTO class_,   // For avoiding Java keyword collision

    @JsonPropertyDescription("Requirement identifier (The unique id of the requirement)")
    String requirementId,

    @JsonPropertyDescription("Requirement text")
    String requirementText
) {
}
