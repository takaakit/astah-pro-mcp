package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDomainWithPhysicalNameDTO(
    @JsonPropertyDescription("Target ER domain identifier")
    String targetERDomainId,

    @JsonPropertyDescription("Physical name")
    String physicalName
) {
}
