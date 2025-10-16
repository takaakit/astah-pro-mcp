package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewSequenceDiagramInPackageDTO(
    @JsonPropertyDescription("Parent package identifier")
    String parentPackageId,
    
    @JsonPropertyDescription("New sequence diagram name")
    String newSequenceDiagramName
) {
}
