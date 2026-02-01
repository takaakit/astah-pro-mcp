package com.astahpromcp.tool.astah.pro.model.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ERDiagramWithNotationDTO(
    @JsonPropertyDescription("Target ER diagram identifier")
    String targetERDiagramId,

    @JsonPropertyDescription("Notation: \"IDEF1X\",\"IE\"")
    String notation
) {
}
