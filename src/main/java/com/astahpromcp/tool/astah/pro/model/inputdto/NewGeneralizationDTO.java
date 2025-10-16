package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewGeneralizationDTO(
        @JsonPropertyDescription("Sub class identifier")
        String subClassId,

        @JsonPropertyDescription("Super class identifier")
        String superClassId
) {
}
