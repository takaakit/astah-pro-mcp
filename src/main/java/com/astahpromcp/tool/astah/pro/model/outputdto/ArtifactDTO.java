package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ArtifactDTO(
    @JsonPropertyDescription("Class information")
    @JsonProperty("class")
    ClassDTO class_   // For avoiding Java keyword collision
) {
}
