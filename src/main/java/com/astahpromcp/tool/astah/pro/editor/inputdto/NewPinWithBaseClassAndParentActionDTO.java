package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewPinWithBaseClassAndParentActionDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Base class identifier")
    String baseClassId,

    @JsonPropertyDescription("Parent action identifier")
    String parentActionId,

    @JsonPropertyDescription("New pin name. An empty string is not allowed as a pin name.")
    String newPinName,

    @JsonPropertyDescription("Is input pin (True: Input pin / False: Output pin)")
    boolean isInput,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
