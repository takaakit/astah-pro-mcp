package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ActionWithCallingActivityDTO(
    @JsonPropertyDescription("Target action identifier")
    String targetActionId,
    
    @JsonPropertyDescription("Calling activity identifier")
    String callingActivityId
) {
}
