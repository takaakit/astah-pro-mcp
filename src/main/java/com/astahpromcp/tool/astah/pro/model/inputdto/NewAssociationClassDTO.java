package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewAssociationClassDTO(
        @JsonPropertyDescription("Source class identifier")
        String sourceClassId,

        @JsonPropertyDescription("Target class identifier")
        String targetClassId,

        @JsonPropertyDescription("New association class name")
        String newAssociationClassName
) {
}
