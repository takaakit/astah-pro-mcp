package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MessageWithOperationDTO(
    @JsonPropertyDescription("Target message identifier")
    String targetMessageId,
    
    @JsonPropertyDescription("Operation identifier")
    String operationId
) {
}
