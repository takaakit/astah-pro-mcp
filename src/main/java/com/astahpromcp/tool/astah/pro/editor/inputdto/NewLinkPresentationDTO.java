package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLinkPresentationDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Target element identifier")
    String targetElementId,

    @JsonPropertyDescription("Source node presentation identifier")
    String sourceNodePresentationId,

    @JsonPropertyDescription("Target node presentation identifier")
    String targetNodePresentationId
) {
}
