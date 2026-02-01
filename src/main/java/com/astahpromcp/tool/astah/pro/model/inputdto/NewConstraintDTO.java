package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewConstraintDTO(
        @JsonPropertyDescription("Target named element identifier")
        String targetNamedElementId,

        @JsonPropertyDescription("New constraint name")
        String newConstraintName
) {
}
