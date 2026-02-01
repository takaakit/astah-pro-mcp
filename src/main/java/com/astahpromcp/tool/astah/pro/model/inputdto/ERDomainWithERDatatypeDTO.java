package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDomainWithERDatatypeDTO(
    @JsonPropertyDescription("Target ER domain identifier")
    String targetERDomainId,

    @JsonPropertyDescription("ER datatype identifier")
    String erDatatypeId
) {
}
