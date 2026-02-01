package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERIndexWithUniqueDTO(
    @JsonPropertyDescription("Target ER index identifier")
    String targetERIndexId,

    @JsonPropertyDescription("Is unique: true - Alternate Key(AK), false - Inversion Entry(IE)")
    boolean isUnique
) {
}
