package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewAttributeInClassDTO(
    @JsonPropertyDescription("Parent class identifier")
    String parentClassId,

    @JsonPropertyDescription("New attribute name")
    String newAttributeName
) {
}
