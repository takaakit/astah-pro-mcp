package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkEndWithAggregationDTO(
    @JsonPropertyDescription("Target link end identifier")
    String targetLinkEndId,
    
    @JsonPropertyDescription("Is Aggregation")
    boolean isAggregation
) {
}
