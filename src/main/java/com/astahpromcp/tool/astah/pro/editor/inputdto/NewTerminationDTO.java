package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewTerminationDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("Target node presentation identifier. Must be one of the following node presentation types: Lifeline.")
    String targetNodePresentationId
) {
}
