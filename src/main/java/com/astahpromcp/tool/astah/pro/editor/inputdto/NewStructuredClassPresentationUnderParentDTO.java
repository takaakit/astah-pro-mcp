package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewStructuredClassPresentationUnderParentDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Target element identifier")
    String targetElementId,

    @JsonPropertyDescription("Parent node presentation identifier")
    String parentNodePresentationId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
