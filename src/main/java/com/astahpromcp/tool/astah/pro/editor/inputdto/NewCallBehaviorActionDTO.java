package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewCallBehaviorActionDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Activity diagram identifier to reference")
    String referenceActivityDiagramId,

    @JsonPropertyDescription("New call behavior action name. An empty string is not allowed as an action name.")
    String newCallBehaviorActionName,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
