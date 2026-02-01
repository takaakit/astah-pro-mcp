package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERSubtypeRelationshipWithConclusiveDTO(
    @JsonPropertyDescription("Target ER subtype relationship identifier")
    String targetERSubtypeRelationshipId,

    @JsonPropertyDescription("Is conclusive. If true, it is conclusive, otherwise, it is not conclusive.")
    boolean isConclusive
) {
}
