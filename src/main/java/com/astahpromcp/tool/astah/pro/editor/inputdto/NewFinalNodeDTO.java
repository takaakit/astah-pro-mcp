package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewFinalNodeDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("New final node name. An empty string is not allowed as a node name.")
    String newFinalNodeName,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
