package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERRelationshipWithParentVerbPhraseDTO(
    @JsonPropertyDescription("Target ER relationship identifier")
    String targetERRelationshipId,

    @JsonPropertyDescription("Verb phrase (Parent to Child)")
    String parentVerbPhrase
) {
}
