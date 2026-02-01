package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERAttributeWithERDomainDTO(
    @JsonPropertyDescription("Target ER attribute identifier")
    String targetERAttributeId,

    @JsonPropertyDescription("ER domain identifier")
    String erDomainId
) {
}
