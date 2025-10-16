package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewCreateMessageDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New create message name. '()' is appended to the message name when it is displayed. Therefore, it is not necessary to add '()' at the end of the message name.")
    String newCreateMessageName,
    
    @JsonPropertyDescription("Sender node presentation identifier")
    String senderNodePresentationId,
    
    @JsonPropertyDescription("Receiver node presentation identifier")
    String receiverNodePresentationId,
    
    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
