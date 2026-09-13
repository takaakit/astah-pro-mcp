package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLifelineDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New lifeline name. Set this to an empty string when the lifeline has no name.")
    String newLifelineName,
    
    @JsonPropertyDescription("Location X coordinate")
    int locationX
) {
}
