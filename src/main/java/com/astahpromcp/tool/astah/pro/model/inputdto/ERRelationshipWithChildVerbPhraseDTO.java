package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERRelationshipWithChildVerbPhraseDTO(
    @JsonPropertyDescription("Target ER relationship identifier")
    String targetERRelationshipId,

    @JsonPropertyDescription("Verb phrase (Child to Parent)")
    String childVerbPhrase
) {
}
