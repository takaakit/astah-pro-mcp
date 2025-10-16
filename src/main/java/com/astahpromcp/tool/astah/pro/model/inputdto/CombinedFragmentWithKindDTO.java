package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.CombinedFragmentKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CombinedFragmentWithKindDTO(
    @JsonPropertyDescription("Target combined fragment identifier")
    String targetCombinedFragmentId,
    
    @JsonPropertyDescription("Combined fragment kind")
    CombinedFragmentKind kind
) {
}
