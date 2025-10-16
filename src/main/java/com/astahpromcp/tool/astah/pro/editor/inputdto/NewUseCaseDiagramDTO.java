package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewUseCaseDiagramDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,

    @JsonPropertyDescription("New use case diagram name")
    String newUseCaseDiagramName
) {
}
