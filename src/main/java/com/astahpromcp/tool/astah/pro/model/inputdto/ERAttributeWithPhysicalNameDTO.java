package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithPhysicalNameDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("Physical name")
    String physicalName
) {
}
