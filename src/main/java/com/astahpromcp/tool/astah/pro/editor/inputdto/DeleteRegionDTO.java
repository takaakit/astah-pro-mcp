package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DeleteRegionDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent node presentation identifier")
    String parentNodePresentationId,

    @JsonPropertyDescription("Region index to delete")
    int index
) {
}
