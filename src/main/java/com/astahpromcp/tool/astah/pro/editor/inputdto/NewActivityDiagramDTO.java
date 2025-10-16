package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewActivityDiagramDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,

    @JsonPropertyDescription("New activity diagram name")
    String newActivityDiagramName
) {
}
