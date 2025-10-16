package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewFoundMessageDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New found message name. '()' is appended to the message name when it is displayed. Therefore, it is not necessary to add '()' at the end of the message name.")
    String newFoundMessageName,
    
    @JsonPropertyDescription("Start point X coordinate")
    int startPointX,
    
    @JsonPropertyDescription("Start point Y coordinate")
    int startPointY,
    
    @JsonPropertyDescription("Target receiver node presentation identifier")
    String targetReceiverNodePresentationId
) {
}
