package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDomainWithParentERDomainDTO(
    @JsonPropertyDescription("Target ER domain identifier")
    String targetERDomainId,

    @JsonPropertyDescription("Parent ER domain identifier")
    String parentERDomainId
) {
}
