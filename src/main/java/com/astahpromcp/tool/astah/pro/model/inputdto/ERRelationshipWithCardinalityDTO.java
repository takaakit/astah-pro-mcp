package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERRelationshipWithCardinalityDTO(
    @JsonPropertyDescription("Target ER relationship identifier")
    String targetERRelationshipId,

    @JsonPropertyDescription("Cardinality. ex) 0orMore: \"0orMore\", 1orMore: \"1orMore\", 0or1: \"0or1\", 2: \"2\", n: \"n\"")
    String cardinality
) {
}
