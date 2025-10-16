package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewParameterToOperationDTO(
        @JsonPropertyDescription("Target operation identifier")
        String targetOperationId,

        @JsonPropertyDescription("New parameter name")
        String newParameterName
) {
}
