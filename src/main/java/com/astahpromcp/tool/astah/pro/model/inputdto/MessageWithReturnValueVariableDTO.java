package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MessageWithReturnValueVariableDTO(
    @JsonPropertyDescription("Target message identifier")
    String targetMessageId,
    
    @JsonPropertyDescription("Return value variable")
    String returnValueVariable
) {
}
