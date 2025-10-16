package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewDiagramInPackageDTO(
    @JsonPropertyDescription("Target package identifier")
    String targetPackageId,

    @JsonPropertyDescription("New diagram name")
    String newDiagramName
) {
}
