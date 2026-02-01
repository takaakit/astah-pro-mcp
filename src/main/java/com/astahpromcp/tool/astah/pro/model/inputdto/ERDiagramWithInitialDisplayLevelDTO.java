package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDiagramWithInitialDisplayLevelDTO(
    @JsonPropertyDescription("Target ER diagram identifier")
    String targetERDiagramId,

    @JsonPropertyDescription("Initial Display Level: \"Entity\",\"Primary Key\",\"Attribute\"")
    String initialDisplayLevel
) {
}
