package com.astahpromcp.tool.astah.pro.common.inputdto;

import com.astahpromcp.tool.astah.pro.common.DiagramType;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiagramTypeDTO(
    @JsonPropertyDescription("Diagram type")
    DiagramType diagramType
) {
}
