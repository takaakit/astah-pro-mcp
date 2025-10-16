package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeletePresentationDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,
    
    @JsonPropertyDescription("Delete target presentation identifier")
    String targetPresentationId
) {
}
