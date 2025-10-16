package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ClassDiagramDTO(
    @JsonPropertyDescription("Diagram information")
    DiagramDTO diagram
) {
}
