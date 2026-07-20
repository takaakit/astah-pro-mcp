package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ComponentDTO(
    @JsonPropertyDescription("Class info")
    @JsonProperty("class")
    ClassDTO class_   // For avoiding Java keyword collision
) {
}
