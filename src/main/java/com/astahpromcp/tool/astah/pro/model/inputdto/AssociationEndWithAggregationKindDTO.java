package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.AggregationKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AssociationEndWithAggregationKindDTO(
    @JsonPropertyDescription("Target association end identifier")
    String targetAssociationEndId,

    @JsonPropertyDescription("Aggregation kind")
    AggregationKind aggregationKind
) {
}
