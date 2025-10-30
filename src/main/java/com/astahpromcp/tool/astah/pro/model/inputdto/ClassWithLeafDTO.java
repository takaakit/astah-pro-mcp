package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassWithLeafDTO(
    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("Is Leaf")
    boolean isLeaf
) {
}
