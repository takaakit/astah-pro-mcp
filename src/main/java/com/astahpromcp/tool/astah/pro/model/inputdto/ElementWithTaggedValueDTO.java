package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ElementWithTaggedValueDTO(
    @JsonPropertyDescription("Target element identifier")
    String targetElementId,

    @JsonPropertyDescription("Target key of tagged value")
    String targetKey,

    @JsonPropertyDescription("Value")
    String value
) {
}
