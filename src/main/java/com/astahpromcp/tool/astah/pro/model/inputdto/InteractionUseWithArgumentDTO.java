package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record InteractionUseWithArgumentDTO(
    @JsonPropertyDescription("Target interaction use identifier")
    String targetInteractionUseId,

    @JsonPropertyDescription("Argument")
    String argument
) {
}
