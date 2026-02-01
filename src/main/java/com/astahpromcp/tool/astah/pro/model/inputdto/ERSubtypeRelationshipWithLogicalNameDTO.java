package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERSubtypeRelationshipWithLogicalNameDTO(
    @JsonPropertyDescription("Target ER subtype relationship identifier")
    String targetERSubtypeRelationshipId,

    @JsonPropertyDescription("Logical name")
    String logicalName
) {
}
