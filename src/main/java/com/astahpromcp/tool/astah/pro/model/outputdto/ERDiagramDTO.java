package com.astahpromcp.tool.astah.pro.model.outputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ERDiagramDTO(
    @JsonPropertyDescription("Diagram information")
    DiagramDTO diagram,

    @JsonPropertyDescription("Initial Display Level: \"Entity\",\"Primary Key\",\"Attribute\"")
    String initialDisplayLevel,

    @JsonPropertyDescription("Model Type: \"Logical Model\",\"Physical Model\"")
    String modelType,

    @JsonPropertyDescription("Notation: \"IDEF1X\",\"IE\"")
    String notation,

    @JsonPropertyDescription("Check if the ER diagram is Align Attribute Items.")
    boolean isAlignAttributeItems
) {
}
