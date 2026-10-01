package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.astahpromcp.tool.astah.pro.common.AggregationKind;
import com.astahpromcp.tool.astah.pro.common.NavigabilityKind;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewAssociationDTO(
        @JsonPropertyDescription("Source class identifier")
        String sourceClassId,

        @JsonPropertyDescription("Target class identifier")
        String targetClassId,

        @JsonPropertyDescription("Navigability of the source association end")
        NavigabilityKind sourceNavigability,

        @JsonPropertyDescription("Navigability of the target association end")
        NavigabilityKind targetNavigability,

        @JsonPropertyDescription("Aggregation kind of the source association end. The diamond is placed at the source class, and is not shown if sourceNavigability is 'navigable'.")
        AggregationKind sourceAggregationKind,

        @JsonPropertyDescription("Aggregation kind of the target association end. The diamond is placed at the target class, and is not shown if targetNavigability is 'navigable'.")
        AggregationKind targetAggregationKind
) {
}
