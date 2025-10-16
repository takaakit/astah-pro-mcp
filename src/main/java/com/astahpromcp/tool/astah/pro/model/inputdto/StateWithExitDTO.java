package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StateWithExitDTO(
    @JsonPropertyDescription("Target state ID")
    String targetStateId,

    @JsonPropertyDescription("Exit")
    String exit
) {
}
