package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithTypeDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,

    @JsonPropertyDescription("Attribute type identifier")
    String attributeTypeId
) {
}
