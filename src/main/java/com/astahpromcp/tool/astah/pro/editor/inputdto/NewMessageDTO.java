package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewMessageDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("New message name. '()' is appended to the message name when it is displayed. Therefore, it is not necessary to add '()' at the end of the message name.")
    String newMessageName,
    
    @JsonPropertyDescription("Sender node presentation identifier. Must be one of the following node presentation types: Activation (ExecutionSpecification), Lifeline, InteractionUse, or Frame.")
    String senderNodePresentationId,
    
    @JsonPropertyDescription("Receiver node presentation identifier. Must be one of the following node presentation types: Lifeline, InteractionUse, or Frame.")
    String receiverNodePresentationId,
    
    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
