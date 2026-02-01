package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERSubtypeRelationshipWithPhysicalNameDTO(
    @JsonPropertyDescription("Target ER subtype relationship identifier")
    String targetERSubtypeRelationshipId,

    @JsonPropertyDescription("Physical name")
    String physicalName
) {
}
