package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewCombinedFragmentDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New combined fragment name")
    String newCombinedFragmentName,
    
    @JsonPropertyDescription("Combined fragment kind")
    CombinedFragmentKind combinedFragmentKind,
    
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
