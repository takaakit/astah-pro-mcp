package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERRelationshipWithParentVerbPhraseDTO(
    @JsonPropertyDescription("Target ER relationship identifier")
    String targetERRelationshipId,

    @JsonPropertyDescription("Parent verb phrase")
    String parentVerbPhrase
) {
}
