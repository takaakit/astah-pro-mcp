package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPortPresentationDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Target node presentation identifier. Must be one of the following node presentation types: Part, or StructuredClass.")
    String targetNodePresentationId,

    @JsonPropertyDescription("Target port identifier")
    String targetPortId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
