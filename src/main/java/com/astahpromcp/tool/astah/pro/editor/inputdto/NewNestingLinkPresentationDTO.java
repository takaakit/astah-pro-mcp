package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewNestingLinkPresentationDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent node presentation identifier")
    String parentNodePresentationId,

    @JsonPropertyDescription("Child node presentation identifier")
    String childNodePresentationId
) {
}
