package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EREntityWithLogicalNameDTO(
    @JsonPropertyDescription("Target ER entity identifier")
    String targetEREntityId,

    @JsonPropertyDescription("Logical name")
    String logicalName
) {
}
