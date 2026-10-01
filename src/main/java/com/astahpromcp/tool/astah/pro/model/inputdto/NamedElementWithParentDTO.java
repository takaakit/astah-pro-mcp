package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NamedElementWithParentDTO(
        @JsonPropertyDescription("Target named element identifier")
        String targetNamedElementId,

        @JsonPropertyDescription("New parent named element identifier")
        String newParentNamedElementId
) {
}
