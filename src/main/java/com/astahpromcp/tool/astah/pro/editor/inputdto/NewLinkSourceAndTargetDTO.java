package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewLinkSourceAndTargetDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Source node presentation identifier")
    String sourceNodePresentationId,

    @JsonPropertyDescription("Target node presentation identifier")
    String targetNodePresentationId
) {
}
