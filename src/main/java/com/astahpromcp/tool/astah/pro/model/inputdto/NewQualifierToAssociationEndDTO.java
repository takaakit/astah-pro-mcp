package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewQualifierToAssociationEndDTO(
        @JsonPropertyDescription("Target association end identifier")
        String targetAssociationEndId,

        @JsonPropertyDescription("Qualifier type identifier")
        String qualifierTypeId,

        @JsonPropertyDescription("New qualifier name")
        String newQualifierName
) {
}
