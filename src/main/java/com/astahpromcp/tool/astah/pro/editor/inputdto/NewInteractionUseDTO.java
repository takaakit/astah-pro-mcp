package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewInteractionUseDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New interaction use name")
    String newInteractionUseName,
    
    @JsonPropertyDescription("Location X coordinate")
    int locationX,
    
    @JsonPropertyDescription("Location Y coordinate")
    int locationY,
    
    @JsonPropertyDescription("Width")
    int width,
    
    @JsonPropertyDescription("Height")
    int height
) {
}
