package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassWithActiveDTO(
    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("Is Active")
    boolean isActive
) {
}
