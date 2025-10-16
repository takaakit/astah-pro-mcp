package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewFlowFinalNodeDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("New flow final node name. An empty string is not allowed as a node name.")
    String newFlowFinalNodeName,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
