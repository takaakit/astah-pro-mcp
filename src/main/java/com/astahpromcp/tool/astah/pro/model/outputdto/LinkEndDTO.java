package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LinkEndDTO(
        @JsonPropertyDescription("Named element information")
        NamedElementDTO namedElement,

        @JsonPropertyDescription("Is Aggregation")
        boolean isAggregation,

        @JsonPropertyDescription("Is Composition")
        boolean isComposition,

        @JsonPropertyDescription("Is Navigation")
        boolean isNavigation
) {
}
