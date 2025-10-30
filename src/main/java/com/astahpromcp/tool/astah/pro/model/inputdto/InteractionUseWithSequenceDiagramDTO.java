package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record InteractionUseWithSequenceDiagramDTO(
    @JsonPropertyDescription("Target interaction use identifier")
    String targetInteractionUseId,

    @JsonPropertyDescription("Target sequence diagram identifier to be referred to")
    String targetSequenceDiagramId
) {
}
