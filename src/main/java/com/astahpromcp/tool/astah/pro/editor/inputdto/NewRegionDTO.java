package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewRegionDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent node presentation identifier.")
    String parentNodePresentationId,

    @JsonPropertyDescription("Is Horizontal")
    boolean isHorizontal
) {
}
