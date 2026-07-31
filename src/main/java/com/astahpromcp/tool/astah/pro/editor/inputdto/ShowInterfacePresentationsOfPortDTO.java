package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ShowInterfacePresentationsOfPortDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Target node presentation identifier. Must be one of the following node presentation types: Port.")
    String targetNodePresentationId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
