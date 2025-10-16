package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AttributeWithStaticDTO(
    @JsonPropertyDescription("Target attribute identifier")
    String targetAttributeId,

    @JsonPropertyDescription("Is Static")
    boolean isStatic
) {
}
