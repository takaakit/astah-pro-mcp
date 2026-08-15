package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewReturnMessageDTO(
    @JsonPropertyDescription("Target sequence diagram identifier")
    String targetSequenceDiagramId,
    
    @JsonPropertyDescription("Target message link presentation identifier corresponding to the return message")
    String targetMessageLinkPresentationId,
    
    @JsonPropertyDescription("New return message name")
    String newReturnMessageName
) {
}
