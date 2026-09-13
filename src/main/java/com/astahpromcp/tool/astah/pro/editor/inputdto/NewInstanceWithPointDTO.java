package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewInstanceWithPointDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target class identifier")
    String targetClassId,

    @JsonPropertyDescription("New instance name. Set this to an empty string when the instance specification has no name.")
    String newInstanceName,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
