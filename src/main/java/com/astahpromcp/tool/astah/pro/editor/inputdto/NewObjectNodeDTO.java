package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewObjectNodeDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Base class identifier")
    String baseClassId,

    @JsonPropertyDescription("New object node name. An empty string is not allowed as a node name.")
    String newObjectNodeName,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
