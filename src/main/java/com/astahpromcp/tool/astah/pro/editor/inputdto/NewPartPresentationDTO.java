package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPartPresentationDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Target attribute identifier. It must be an attribute owned by the class that the parent node presentation represents, and must not be a port.")
    String targetAttributeId,

    @JsonPropertyDescription("Parent node presentation identifier. Must be one of the following node presentation types: StructuredClass.")
    String parentNodePresentationId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
