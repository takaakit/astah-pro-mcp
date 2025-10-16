package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record MessageWithArgumentDTO(
    @JsonPropertyDescription("Target message identifier")
    String targetMessageId,
    
    @JsonPropertyDescription("Message argument (shown in the parentheses right after the message name)")
    String argument
) {
}
