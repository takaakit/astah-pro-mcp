package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDomainWithDefaultValueDTO(
    @JsonPropertyDescription("Target ER domain identifier")
    String targetERDomainId,

    @JsonPropertyDescription("Default value")
    String defaultValue
) {
}
