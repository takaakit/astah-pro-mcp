package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewFlowDTO(
    @JsonPropertyDescription("Target activity diagram identifier")
    String targetActivityDiagramId,

    @JsonPropertyDescription("Source node presentation identifier")
    String sourceNodePresentationId,

    @JsonPropertyDescription("Target node presentation identifier")
    String targetNodePresentationId
) {
}
