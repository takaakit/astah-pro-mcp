package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record InteractionOperandIndexWithHeightDTO(
    @JsonPropertyDescription("Target combined fragment identifier")
    String targetCombinedFragmentId,

    @JsonPropertyDescription("1-based index of the target interaction operand within the combined fragment")
    int targetInteractionOperandIndex,

    @JsonPropertyDescription("Height value to set for the interaction operand region")
    int height
) {
}
