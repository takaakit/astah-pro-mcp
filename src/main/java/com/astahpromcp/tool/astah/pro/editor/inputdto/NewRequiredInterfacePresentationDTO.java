package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRequiredInterfacePresentationDTO(
    @JsonPropertyDescription("Target composite structure diagram identifier")
    String targetCompositeStructureDiagramId,

    @JsonPropertyDescription("Target node presentation identifier. Must be one of the following node presentation types: Port, or Part.")
    String targetNodePresentationId,

    @JsonPropertyDescription("Target interface identifier")
    String targetInterfaceId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
