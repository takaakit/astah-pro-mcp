package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewInteractionOperandDTO(
    @JsonPropertyDescription("Target combined fragment identifier")
    String targetCombinedFragmentId,
    
    @JsonPropertyDescription("New interaction operand name")
    String newInteractionOperandName,
    
    @JsonPropertyDescription("Guard condition")
    String guard
) {
}
