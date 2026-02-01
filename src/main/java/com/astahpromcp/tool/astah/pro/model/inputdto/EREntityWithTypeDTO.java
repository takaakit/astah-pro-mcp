package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EREntityWithTypeDTO(
    @JsonPropertyDescription("Target ER entity identifier")
    String targetEREntityId,

    @JsonPropertyDescription("Type for ER Entity. It should be \"Event\", \"Resource\",\"Summary\" or \"\".")
    String type
) {
}
