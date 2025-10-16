package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StateWithDoActivityDTO(
    @JsonPropertyDescription("Target state ID")
    String targetStateId,

    @JsonPropertyDescription("DoActivity")
    String doActivity
) {
}
