package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERIndexWithERAttributeDTO(
    @JsonPropertyDescription("Target ER index identifier")
    String targetERIndexId,

    @JsonPropertyDescription("ER attribute identifier")
    String erAttributeId
) {
}
