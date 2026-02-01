package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EREntityWithPhysicalNameDTO(
    @JsonPropertyDescription("Target ER entity identifier")
    String targetEREntityId,

    @JsonPropertyDescription("Physical name")
    String physicalName
) {
}
