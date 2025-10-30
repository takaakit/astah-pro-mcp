package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassWithAbstractDTO(
    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("Is Abstract")
    boolean isAbstract
) {
}
