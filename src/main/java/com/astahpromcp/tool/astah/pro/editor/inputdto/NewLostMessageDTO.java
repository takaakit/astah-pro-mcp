package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLostMessageDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New lost message name. '()' is appended to the message name when it is displayed. Therefore, it is not necessary to add '()' at the end of the message name.")
    String newLostMessageName,
    
    @JsonPropertyDescription("Target sender node presentation identifier")
    String targetSenderNodePresentationId,
    
    @JsonPropertyDescription("End point X coordinate")
    int endPointX,
    
    @JsonPropertyDescription("End point Y coordinate")
    int endPointY
) {
}
