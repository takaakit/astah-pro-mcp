package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record LifelineWithLengthDTO(
    @JsonPropertyDescription("Target lifeline identifier")
    String targetLifelineId,

    @JsonPropertyDescription("Lifeline length value to set on the lifeline node presentation")
    int length
) {
}
