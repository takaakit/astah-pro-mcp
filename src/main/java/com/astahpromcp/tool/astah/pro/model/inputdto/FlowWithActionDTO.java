package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record FlowWithActionDTO(
    @JsonPropertyDescription("Target flow identifier")
    String targetFlowId,
    
    @JsonPropertyDescription("Action")
    String action
) {
}
