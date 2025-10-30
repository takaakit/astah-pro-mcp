package com.astahpromcp.tool.astah.pro.editor.inputdto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NewDeepHistoryPseudostateDTO(
    @JsonPropertyDescription("Target diagram identifier")
    String targetDiagramId,

    @JsonPropertyDescription("Parent node presentation identifier. If there is no parent node presentation (i.e., when rendering at the top level), set an empty string.")
    String parentNodePresentationId,

    @JsonPropertyDescription("Location X coordinate")
    int locationX,

    @JsonPropertyDescription("Location Y coordinate")
    int locationY
) {
}
