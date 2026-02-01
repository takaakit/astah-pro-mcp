package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERRelationshipWithERIndexDTO(
    @JsonPropertyDescription("Target ER relationship identifier")
    String targetERRelationshipId,

    @JsonPropertyDescription("ER index identifier")
    String erIndexId
) {
}
