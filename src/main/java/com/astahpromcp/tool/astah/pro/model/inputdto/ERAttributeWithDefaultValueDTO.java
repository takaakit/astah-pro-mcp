package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithDefaultValueDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("Default value")
    String defaultValue
) {
}
