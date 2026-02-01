package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewERDiagramDTO(
    @JsonPropertyDescription("Target ER package identifier")
    String targetERPackageId,

    @JsonPropertyDescription("New ER diagram name")
    String newERDiagramName
) {
}
