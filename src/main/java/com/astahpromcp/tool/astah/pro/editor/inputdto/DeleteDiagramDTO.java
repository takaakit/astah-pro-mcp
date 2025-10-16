package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeleteDiagramDTO(
    @JsonPropertyDescription("Delete target diagram identifier")
    String targetDiagramId
) {
}
