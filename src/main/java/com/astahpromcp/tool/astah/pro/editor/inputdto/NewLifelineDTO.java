package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLifelineDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New lifeline name")
    String newLifelineName,
    
    @JsonPropertyDescription("Location X coordinate")
    int locationX
) {
}
