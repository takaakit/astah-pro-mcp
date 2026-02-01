package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDiagramWithModelTypeDTO(
    @JsonPropertyDescription("Target ER diagram identifier")
    String targetERDiagramId,

    @JsonPropertyDescription("Model Type: \"Logical Model\",\"Physical Model\"")
    String modelType
) {
}
