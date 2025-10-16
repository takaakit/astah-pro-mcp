package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record FlowWithGuardDTO(
    @JsonPropertyDescription("Target flow identifier")
    String targetFlowId,
    
    @JsonPropertyDescription("Guard condition. '[]' is appended to the guard condition when it is displayed. Therefore, it is not necessary to add '[]' to the guard condition.")
    String guard
) {
}
